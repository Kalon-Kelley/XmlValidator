package sbccunittest;

import static java.lang.System.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.junit.Assert.*;
import static sbcc.Core.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.junit.*;

import xmlvalidator.*;

/* 10/2/2018 */

public class XmlValidatorTester {

	// The following tests will be done on a locally created String rather than the
	// contents of a
	// downloaded file
	// they have proprietary names but are not case-sensitive
	public ArrayList<String> localStrings = new ArrayList<String>(Arrays.asList("Valid File", "Big Valid File",
			"Unclosed Tag at End", "Orphan Closing Tag", "Attribute Not Quoted", "Unclosed Tag"));

	// The following tests will be done on a downloaded file, not a locally created
	// String
	public ArrayList<String> fileStrings = new ArrayList<String>();

	BasicXmlValidator validator;

	BasicXmlTagStack stack;

	HashMap<String, String> testStrings;

	Random randomGenerator = new Random();

	ArrayList<String> possibleAttributes = new ArrayList<String>(
			Arrays.asList("Version", "default", "pattern", "value", "color", "property", "name", "outfile"));

	ArrayList<String> possibleValues = new ArrayList<String>(
			Arrays.asList("1.0", "dark", "dd/mm/yyyy", "503", "#FFFFFF", "primaryID", "tagName", "file.dat"));

	String standardXMLDeclarationTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	int nestLevel = 0; // used to track proper indentation

	int overallNesting = 0; // used to give the tag labels a root-> child

	int lineNumber = 0;

	HashMap<String, String> errorTags = new HashMap<String, String>();

	HashMap<String, String> errorParameters = new HashMap<String, String>();

	HashMap<String, String> errorLines = new HashMap<String, String>();

	String unclosedEndTagParentLine;

	public static int totalScore = 0;

	public static int extraCredit = 0;


	@BeforeClass
	public static void beforeTesting() {
		totalScore = 0;
		extraCredit = 0;
	}


	@AfterClass
	public static void afterTesting() {
		System.out.println("Estimated score (assuming no late penalties, etc.) = " + totalScore);
		System.out.println("Estimated extra credit (assuming on time submission) = " + extraCredit);
	}


	@Before
	public void setUp() throws Exception {
		stack = new BasicXmlTagStack();
		validator = new BasicXmlValidator();

		// Load or Construct Strings for testing
		prepareTestingStrings();
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testPush() {
		int numberOfTagsToTest = randomGenerator.nextInt(5) + 5;
		XmlTag[] tags = new XmlTag[numberOfTagsToTest];

		for (int i = 0; i < numberOfTagsToTest; i++) {
			var tag = new XmlTag(getRandomString(), randomGenerator.nextInt(100));
			tags[i] = tag;
			stack.push(tag);
		}

		for (int i = 0; i < numberOfTagsToTest; i++) {
			assertEquals(tags[i].name, stack.peek(numberOfTagsToTest - i - 1).name);
			assertEquals(tags[i].index, stack.peek(numberOfTagsToTest - i - 1).index);
		}
		totalScore += 2;
	}


	@Test
	public void testPop() {
		int numberOfTagsToTest = randomGenerator.nextInt(5) + 5;
		XmlTag[] tags = new XmlTag[numberOfTagsToTest];
		for (int i = 0; i < numberOfTagsToTest; i++) {
			var tag = new XmlTag(getRandomString(), randomGenerator.nextInt(100));
			tags[i] = tag;
			stack.push(tag);
		}

		for (int i = numberOfTagsToTest; i > 1; i--) {
			var tag = stack.pop();
			assertEquals(tags[i - 1].name, tag.name);
			assertEquals(tags[i - 1].index, tag.index);
		}
		stack.pop();
		assertEquals(null, stack.pop());
		totalScore += 3;
	}


	@Test
	public void testExercise() {
		int numberOfTagsToTest = randomGenerator.nextInt(5) + 5;
		XmlTag[] tags = new XmlTag[numberOfTagsToTest];
		for (int i = 0; i < numberOfTagsToTest; i++) {
			var tag = new XmlTag(getRandomString(), randomGenerator.nextInt(100));
			tags[i] = tag;
			stack.push(tag);
		}

		assertEquals(tags[numberOfTagsToTest - 1].name, stack.peek(0).name);
		assertEquals(tags[numberOfTagsToTest - 1].index, stack.peek(0).index);
		assertEquals(numberOfTagsToTest, stack.getCount());
		assertEquals(tags[numberOfTagsToTest - 3].name, stack.peek(2).name);
		assertEquals(tags[numberOfTagsToTest - 3].index, stack.peek(2).index);
		var tag = stack.pop();
		assertEquals(tags[numberOfTagsToTest - 1].name, tag.name);
		assertEquals(tags[numberOfTagsToTest - 1].index, tag.index);
		assertEquals(numberOfTagsToTest - 1, stack.getCount());

		for (int i = 1; i < numberOfTagsToTest; i++) {
			tag = stack.pop();
			assertEquals(tags[numberOfTagsToTest - i - 1].name, tag.name);
			assertEquals(tags[numberOfTagsToTest - i - 1].index, tag.index);
		}
		assertEquals(0, stack.getCount());

		stack.pop();
		stack.pop();
		assertEquals(null, stack.pop());
		totalScore += 5;
	}


	@Test
	public void testValidFile() {
		// The BasicXmlValidator has to be able to find the most basic
		// tag mismatch in order to get credit for valid files.
		int numInvalidTests = (int) (10 * Math.random()) + 1;
		for (int i = 0; i < numInvalidTests; i++) {
			List<String> actual = new BasicXmlValidator().validate("<x><y></x>");
			assertEquals("Tag mismatch", actual.get(0));
		}

		// Now test a valid doc
		String xmlDocument = testStrings.get("valid file");
		List<String> result = validator.validate(xmlDocument);
		assertNull(result);
		totalScore += 10;
	}


	@Test
	public void testBigValidFile() throws IOException {
		// The BasicXmlValidator has to be able to find the most basic
		// tag mismatch in order to get credit for valid files.
		int numInvalidTests = (int) (10 * Math.random()) + 1;
		for (int i = 0; i < numInvalidTests; i++) {
			List<String> actual = new BasicXmlValidator().validate("<x><y></x>");
			assertEquals("Tag mismatch", actual.get(0));
		}

		// Now test a valid doc
		String xmlDocument = testStrings.get("big valid file");
		List<String> result = validator.validate(xmlDocument);
		assertNull(result);
		totalScore += 5;
	}


	@Test
	public void testOrphanClosingTag() throws IOException {
		String xmlDocument = testStrings.get("orphan closing tag");
		List<String> result = validator.validate(xmlDocument);
		assertEquals("Orphan closing tag", result.get(0));
		assertEquals(errorTags.get("orphan closing tag"), result.get(1));
		assertEquals(errorLines.get("orphan closing tag"), result.get(2));
		totalScore += 5;
	}


	@Test
	public void testUnclosedTag() throws IOException {
		String xmlDocument = testStrings.get("unclosed tag");
		List<String> result = validator.validate(xmlDocument);
		assertEquals("Tag mismatch", result.get(0));
		assertEquals(errorTags.get("unclosed tag"), result.get(1));
		assertEquals(errorLines.get("unclosed tag"), result.get(2));
		assertEquals(errorParameters.get("unclosed tag"), result.get(3));
		assertEquals(unclosedEndTagParentLine, result.get(4));
		totalScore += 10;
	}


	@Test
	public void testUnclosedTagAtEnd() throws IOException {
		String xmlDocument = testStrings.get("unclosed tag at end");
		List<String> result = validator.validate(xmlDocument);
		assertEquals("Unclosed tag at end", result.get(0));
		assertEquals(errorTags.get("unclosed tag at end"), result.get(1));
		assertEquals(errorLines.get("unclosed tag at end"), result.get(2));
		totalScore += 10;
	}


	@Test
	public void testAttributeNotQuoted() throws IOException {
		String xmlDocument = testStrings.get("attribute not quoted");
		List<String> result = validator.validate(xmlDocument);
		assertEquals("Attribute not quoted", result.get(0));
		assertEquals(errorTags.get("attribute not quoted"), result.get(1));
		assertEquals(errorLines.get("attribute not quoted"), result.get(2));
		assertEquals(errorParameters.get("attribute not quoted"), result.get(3));
		assertEquals(errorLines.get("attribute not quoted"), result.get(4));
		extraCredit += 3;
	}


	public void prepareTestingStrings() throws IOException {
		testStrings = new HashMap<String, String>();
		for (String l : localStrings) {
			testStrings.put(l.toLowerCase(), getLocalString(l));
		}

		for (String l : fileStrings) {
			testStrings.put(l.toLowerCase(), getFileString(l));
		}
	}


	public String getFileString(String testName) throws IOException {
		if (testName.equalsIgnoreCase("unclosed tag")) {
			return readFile("TestFile1.xml");
		}
		if (testName.equalsIgnoreCase("unclosed tag at end")) {
			return readFile("TestFile2.xml");
		}
		if (testName.equalsIgnoreCase("valid file")) {
			return readFile("TestFile3.xml");
		}
		if (testName.equalsIgnoreCase("big valid file")) {
			return readFile("TestFile4.xml");
		}
		if (testName.equalsIgnoreCase("attribute not quoted")) {
			return readFile("TestFile5.xml");
		}
		if (testName.equalsIgnoreCase("orphan closing tag")) {
			return readFile("TestFile6.xml");
		}
		return "";
	}


	public String getLocalString(String testName) {
		if (testName.equalsIgnoreCase("unclosed tag")) {
			return constructXMLWithUnclosedTag(2, 2, true);
		}
		if (testName.equalsIgnoreCase("unclosed tag at end")) {
			return constructXMLWithUnclosedEndTag(2, 2, true);
		}
		if (testName.equalsIgnoreCase("valid file")) {
			return constructValidXMLString(2, 1, true);
		}
		if (testName.equalsIgnoreCase("big valid file")) {
			return constructValidXMLString(5, 2, true, true);
		}
		if (testName.equalsIgnoreCase("attribute not quoted")) {
			return constructXMLWithUnquotedAttribute(5, 2, true);
		}
		if (testName.equalsIgnoreCase("orphan closing tag")) {
			return constructXMLWithOrphanClosingTag(4, 2, true);
		}

		return "";
	}


	public String constructValidXMLString(int minimumNestingLevel, int minimumNumberOfTags, boolean includeAttributes) {
		return constructValidXMLString(minimumNestingLevel, minimumNumberOfTags, includeAttributes, false);
	}


	public String constructValidXMLString(int minimumNestingLevel, int minimumNumberOfTags, boolean includeAttributes,
			boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 0) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, includeComments));
			minimumNumberOfTags--;
		}

		sb.append("\r\n</rootTag>");
		lineNumber++;

		return sb.toString();
	}


	public String constructXMLWithUnclosedTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes) {
		return constructXMLWithUnclosedTag(minimumNestingLevel, minimumNumberOfTags, includeAttributes, false);
	}


	public String constructXMLWithUnclosedTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes, boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 1) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, includeComments));
			minimumNumberOfTags--;
		}
		sb.append(getUnclosedXMLTagTest("unclosed tag"));

		sb.append("\r\n</rootTag>");
		lineNumber++;

		return sb.toString();
	}


	public String constructXMLWithUnclosedEndTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes) {
		return constructXMLWithUnclosedEndTag(minimumNestingLevel, minimumNumberOfTags, includeAttributes, false);
	}


	public String constructXMLWithUnclosedEndTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes, boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 1) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, includeComments));
			minimumNumberOfTags--;
		}
		sb.append("\r\n<unclosedEnd>Content");
		lineNumber++;
		errorTags.put("unclosed tag at end", "unclosedEnd");
		errorLines.put("unclosed tag at end", Integer.toString(lineNumber));

		return sb.toString();
	}


	public String constructXMLWithUnquotedAttribute(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeComments) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		int unquotedAttributesTag = randomGenerator.nextInt(minimumNumberOfTags - 1) + 1;

		while (minimumNumberOfTags > 0) {
			sb.append(getXMLTag(minimumNestingLevel - 1, true, includeComments,
					minimumNumberOfTags != unquotedAttributesTag));
			minimumNumberOfTags--;
		}

		sb.append("\r\n</rootTag>");
		lineNumber++;

		return sb.toString();
	}


	public String constructXMLWithOrphanClosingTag(int minimumNestingLevel, int minimumNumberOfTags,
			boolean includeAttributes) {
		StringBuilder sb = new StringBuilder();
		sb.append(standardXMLDeclarationTag);
		lineNumber = 2;
		overallNesting = minimumNestingLevel;
		sb.append("\r\n<rootTag>");

		while (minimumNumberOfTags > 1) {
			sb.append(getXMLTag(minimumNestingLevel - 1, includeAttributes, false));
			minimumNumberOfTags--;
		}

		sb.append("\r\n</rootTag>");
		lineNumber++;

		errorTags.put("orphan closing tag", "level" + Integer.toString(minimumNestingLevel - 1));
		String extraCloser = "\r\n</" + errorTags.get("orphan closing tag") + ">";
		lineNumber++;
		sb.append(extraCloser);
		errorLines.put("orphan closing tag", Integer.toString(lineNumber));

		return sb.toString();
	}


	public String getXMLTag(int childNestingLevel, boolean includeAttributes) {
		return getXMLTag(childNestingLevel, includeAttributes, false);
	}


	public String getXMLTag(int childNestingLevel, boolean includeAttributes, boolean includeComments) {
		return getXMLTag(childNestingLevel, includeAttributes, includeComments, true);
	}


	public String getXMLTag(int childNestingLevel, boolean includeAttributes, boolean includeComments,
			boolean quoteAttributes) {
		StringBuilder sb = new StringBuilder();
		String tagName = "level" + Integer.toString(overallNesting - childNestingLevel);
		sb.append("\r\n");
		lineNumber++;
		for (int i = 0; i <= nestLevel; i++) {
			sb.append("    ");
		}
		if (includeComments) {
			sb.append("\r\n");
			for (int i = 0; i <= nestLevel; i++) {
				sb.append("    ");
			}
			sb.append("<!--  This is a comment -->\r\n");
			lineNumber += 2;
			for (int i = 0; i <= nestLevel; i++) {
				sb.append("    ");
			}
		}
		sb.append("<").append(tagName).append(" ");
		if (includeAttributes) {
			sb.append(getRandomAttribute(quoteAttributes));
			if (!quoteAttributes) {
				errorTags.put("attribute not quoted", tagName);
				errorLines.put("attribute not quoted", Integer.toString(lineNumber));
				quoteAttributes = true;
			}
		}
		sb.append(">");
		if (childNestingLevel > 0) {
			nestLevel++;
			int children = randomGenerator.nextInt(3) + 1;
			for (int c = 0; c < children; c++) {
				sb.append(getXMLTag(childNestingLevel - 1, includeAttributes));
			}
			nestLevel--;
			sb.append("\r\n");
			lineNumber++;
			for (int i = 0; i <= nestLevel; i++) {
				sb.append("    ");
			}
		} else {
			sb.append("Tag Content");
		}
		sb.append("</").append(tagName).append(">");

		return sb.toString();
	}


	public String getUnclosedXMLTagTest(String test) {
		StringBuilder sb = new StringBuilder();

		errorLines.put(test, Integer.toString((lineNumber += 2)));
		errorTags.put(test, "unclosed");
		errorParameters.put(test, "parentTag");
		unclosedEndTagParentLine = Integer.toString(lineNumber += 2);

		sb.append("\r\n<parentTag>").append("\r\n<unclosed>Content\r\n").append("\r\n</parentTag>");
		lineNumber += 2;

		return sb.toString();
	}


	public String getRandomAttribute() {
		return getRandomAttribute(true);
	}


	public String getRandomAttribute(boolean quoted) {
		StringBuilder sb = new StringBuilder();
		int index = randomGenerator.nextInt(possibleAttributes.size());
		String attributeName = possibleAttributes.get(index);
		index = randomGenerator.nextInt(possibleValues.size());
		String value = possibleValues.get(index);
		sb.append(attributeName).append("=");
		if (quoted) {
			sb.append("\"");
		}
		sb.append(value);
		if (quoted) {
			sb.append("\"");
		} else {
			errorParameters.put("attribute not quoted", attributeName);
		}

		return sb.toString();
	}


	public String getRandomString() {
		return getRandomString(4);
	}


	public String getRandomString(int stringLength) {
		return getRandomString(stringLength, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}


	public String getRandomString(int stringLength, String possibleCharacters) {
		return getRandomString(stringLength, possibleCharacters, possibleCharacters);
	}


	public String getRandomString(int stringLength, String possibleCharacters, String startingCharacters) {
		StringBuilder sb = new StringBuilder(stringLength);
		sb.append(startingCharacters.charAt(randomGenerator.nextInt(startingCharacters.length())));
		for (int i = 1; i < stringLength; i++) {
			sb.append(possibleCharacters.charAt(randomGenerator.nextInt(possibleCharacters.length())));
		}
		return sb.toString();
	}


	@Test
	public void testPmd() {
		try {
			execPmd("src" + File.separator + "xmlvalidator", "cs106.ruleset");
		} catch (Exception ex) {
			fail(ex.getMessage());
		}

		totalScore += 5;

	}


	private static void execPmd(String srcFolder, String rulePath) throws Exception {

		File srcDir = new File(srcFolder);
		File ruleFile = new File(rulePath);

		verifySrcAndRulesExist(srcDir, ruleFile);

		ProcessBuilder pb;
		if (getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
			String pmdBatPath = ".\\pmd_min\\bin\\pmd.bat";
			String curPath = Paths.get(".").toAbsolutePath().toString();

			// Handle CS lab situation where the current dir is a UNC path
			if (curPath.startsWith("\\\\NEBULA\\cloud$")) {
				curPath = "N:\\" + substringAfter(curPath, "cloud$\\");
				pmdBatPath = curPath + pmdBatPath.substring(1);
			}
			pb = new ProcessBuilder(
					pmdBatPath,
					"-f", "text",
					"-d", srcDir.getAbsolutePath(),
					"-R", ruleFile.getAbsolutePath());
		} else {
			pb = new ProcessBuilder(
					"./pmd_min/bin/run.sh", "pmd",
					"-d", srcDir.getAbsolutePath(),
					"-R", ruleFile.getAbsolutePath());
		}
		Process process = pb.start();
		int errCode = process.waitFor();

		switch (errCode) {

		case 1:
			out.println("PMD Check: -5 pts");
			String errorOutput = getOutput(process.getErrorStream());
			fail("Command Error:  " + errorOutput);
			break;

		case 4:
			out.println("PMD Check: -5 pts");
			String output = getOutput(process.getInputStream());
			fail(trimFullClassPaths(output));
			break;

		}

	}


	private static String trimFullClassPaths(String output) {
		// Shorten output to just the short class name, line, and error.
		String[] lines = output.split(getProperty("line.separator"));
		StringBuilder sb = new StringBuilder();
		for (String line : lines)
			sb.append(substringAfterLast(line, File.separator)).append(lineSeparator());

		String trimmedOutput = sb.toString();
		return trimmedOutput;
	}


	private static void verifySrcAndRulesExist(File fileFolderToCheck, File ruleFile) throws Exception {
		if (!fileFolderToCheck.exists())
			throw new FileNotFoundException(
					"The folder to check '" + fileFolderToCheck.getAbsolutePath() + "' does not exist.");

		if (!fileFolderToCheck.isDirectory())
			throw new FileNotFoundException(
					"The folder to check '" + fileFolderToCheck.getAbsolutePath() + "' is not a directory.");

		if (!ruleFile.exists())
			throw new FileNotFoundException(
					"The rule set file '" + ruleFile.getAbsolutePath() + "' could not be found.");
	}


	private static String getOutput(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + getProperty("line.separator"));
			}
		} finally {
			br.close();
		}
		return sb.toString();

	}

}