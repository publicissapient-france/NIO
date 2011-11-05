/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package fr.xebia.usi.quizz.web.netty;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 *
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class HttpServer {
    public static void main(String[] args) {
        // Configure the server.
        int nbThread = 4;
                int nbListeningPort = 1;

                ThreadFactory bossThreadFactory = new ThreadFactory() {

                    private int i = 1;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("BossExec #1-" + i++);
                        return thread;
                    }
                };

                ThreadFactory ioThreadFactory = new ThreadFactory() {

                    private int i = 1;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("BossExec #1-" + i++);
                        return thread;
                    }
                };
                // Configure the server.
                //ExecutorService bossExec = Executors.newFixedThreadPool(20, threadFactory);
                //ExecutorService bossExec = Executors.newCachedThreadPool();
                //ExecutorService ioExec = Executors.newCachedThreadPool();
                ExecutorService bossExec = new OrderedMemoryAwareThreadPoolExecutor(nbListeningPort, 400000000, 2000000000, 60, TimeUnit.SECONDS, bossThreadFactory);
                ExecutorService ioExec = new OrderedMemoryAwareThreadPoolExecutor(nbThread, 400000000, 2000000000, 60, TimeUnit.SECONDS, ioThreadFactory);
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(bossExec, ioExec, 4));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
        // A priori beaucoup de pb de connection reset by peer sous macos sans ces options
        bootstrap.setOption("child.tcpNoDelay", true);
        //bootstrap.setOption("child.keepAlive", false);
        bootstrap.setOption("backlog", 1000);
        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8080));
    }
}
