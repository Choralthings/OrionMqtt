package general;

public class InvalidThreadException extends RuntimeException {

	/* constructors */
	public InvalidThreadException(String s) {
		//System.out.println(s);
	}
	public InvalidThreadException() {
		this("Cannot be executed from this Thread with a private semaphore");
	}

}