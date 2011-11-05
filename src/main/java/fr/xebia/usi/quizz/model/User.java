package fr.xebia.usi.quizz.model;

public class User {

	private String email;
	
	private String firstname;
	
	private String lastname;
	
	private String password;

	/**
	 * @return the email
	 */
	public String getMail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setMail(String email) {
		this.email = email;
	}

	/**
	 * @return the firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	/**
	 * @param firstname the firstname to set
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return lastname;
	}

	/**
	 * @param lastname the lastname to set
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean isValidForSave(){
		return (this.email != null && !this.email.trim().isEmpty()) && 
		(this.firstname != null && !this.firstname.trim().isEmpty()) &&
		(this.lastname != null && !this.lastname.trim().isEmpty()) &&
		(this.password != null && !this.password.trim().isEmpty());
	}
}
