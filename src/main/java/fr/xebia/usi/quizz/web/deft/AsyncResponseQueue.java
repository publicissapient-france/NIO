package fr.xebia.usi.quizz.web.deft;

import java.util.concurrent.LinkedBlockingQueue;

import org.deftserver.io.DefaultIOWorkerLoop;
import org.deftserver.io.IOLoop;
import org.deftserver.io.timeout.Timeout;
import org.deftserver.web.AsyncCallback;
import org.deftserver.web.http.HttpResponse;

public class AsyncResponseQueue {

	private final LinkedBlockingQueue<HttpResponse> queue;
	
	
	private Boolean planned;

	public AsyncResponseQueue() {
		queue = new LinkedBlockingQueue<HttpResponse>();
		planned = false;
	}

	public void pushResponseToSend(HttpResponse response) {
		try {
			queue.put(response);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (!queue.isEmpty() && ! planned){
			addTimeout();
			synchronized (planned) {
				planned = true;
			}
		}
	}

	public void sendQueuedResponses() {
		HttpResponse resp;
		while ((resp = queue.poll()) != null) {
			resp.finish();
		}

		synchronized (planned) {
			planned = false;
		}
	}

	private void addTimeout() {
		IOLoop.INSTANCE.addTimeout(new Timeout(10, new AsyncCallback() {

			@Override
			public void onCallback() {
				sendQueuedResponses();
			}
		}));
	}
}
