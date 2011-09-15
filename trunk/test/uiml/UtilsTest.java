package uiml;
import junit.framework.TestCase;


public class UtilsTest extends TestCase {

	public void testSplit() {
		String[] result;

		result = UIMLFactory.split(null, ',');
		assertTrue(result.length == 0);

		result = UIMLFactory.split("a,b,c,d,e", ',');
		assertTrue(result.length == 5);

		result = UIMLFactory.split("a,b,c,,e", ',');
		assertTrue(result.length == 4);

		result = UIMLFactory.split("a,b,c,,", ',');
		assertTrue(result.length == 3);
	}

	public void testTokenize() {
		String[] result;

		result = UIMLFactory.tokenize(null, ',');
		assertTrue(result.length == 0);

		result = UIMLFactory.tokenize("a,b,c,d,e", ',');
		assertTrue(result.length == 5);

		result = UIMLFactory.tokenize("a,b,c,,e", ',');
		assertTrue(result.length == 5);

		result = UIMLFactory.tokenize("a,b,c,,", ',');
		assertTrue(result.length == 5);

		result = UIMLFactory.tokenize("a,b,c,,,", ',');
		assertTrue(result.length == 6);
		assertTrue(result[0].length() == 1);
		assertTrue(result[1].length() == 1);
		assertTrue(result[2].length() == 1);
		assertTrue(result[3].length() == 0);
		assertTrue(result[4].length() == 0);
		assertTrue(result[5].length() == 0);
	}
}
