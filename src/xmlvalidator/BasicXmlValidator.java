package xmlvalidator;

import static org.apache.commons.lang3.StringUtils.*;
import static sbcc.Core.*;

import java.io.*;
import java.util.*;

/*
 * 
 *  AUTHOR = Lucas Kelley
 * 
 */

public class BasicXmlValidator implements XmlValidator {

	/*
	 * Creating public variables for tracking at which index a tag is open or
	 * closed, if an index of a tag has an attribute or not and the name of the
	 * attribute that doesnt have " "
	 */
	public ArrayList<Integer> tagOpenClose = new ArrayList<Integer>();
	public ArrayList<Integer> hasAttribute = new ArrayList<Integer>();
	public String attributeName = null;

	/*
	 * Creating private findAllTags method that takes a list of strings as an input
	 * and returns an ArrayList of XmlTag objects
	 */
	private ArrayList<XmlTag> findAllTags(List<String> lines) {
		ArrayList<XmlTag> allXmlTags = new ArrayList<XmlTag>();
		// Creating a for loop that loops the same number of times as there are lines in
		// the string
		for (int line = 0; line < lines.size(); line++) {
			String[] substrings = substringsBetween(lines.get(line), "<", ">");
			// Testing if the array is not empty
			if (substrings != null) {
				List<String> tags = new ArrayList<String>(Arrays.asList(substrings));
				/*
				 * Creating a for loop that loops the same number of times as there are tags per
				 * line
				 */
				for (int tag = 0; tag < tags.size(); tag++) {
					String name = extractName(tags.get(tag));
					// Testing if the name of the tag is not null
					if (name != null) {
						XmlTag xmlTag = new XmlTag(name, line + 1);
						allXmlTags.add(xmlTag);
					}
				}
			}
		}
		return allXmlTags;
	}


	/*
	 * Creating an extractName method that takes a string input and returns a string
	 */
	private String extractName(String braceEnclosedText) {
		// Initializing a string called name
		String name = null;
		/*
		 * Testing if the string has a letter in the first index and does not contain a
		 * space
		 */
		if (Character.isLetter(braceEnclosedText.charAt(0)) && !braceEnclosedText.contains(" ")) {
			name = braceEnclosedText;
			this.tagOpenClose.add(0);
			this.hasAttribute.add(0);
			/*
			 * Testing if the string has a letter in the first index and does not contain a
			 * space and does not end with a /
			 */
		} else if (Character.isLetter(braceEnclosedText.charAt(0)) && braceEnclosedText.contains(" ")
				&& !braceEnclosedText.endsWith("/")) {
			/*
			 * Testing if the string contains a = and doesnt have any " for an unquoted
			 * attribute
			 */
			if (braceEnclosedText.contains("=") && !braceEnclosedText.contains("\"")) {
				this.attributeName = substringBetween(braceEnclosedText, " ", "=");
				this.hasAttribute.add(1);
				// Else the attribute is quoted
			} else {
				this.hasAttribute.add(0);
			}
			name = substringBefore(braceEnclosedText, " ");
			this.tagOpenClose.add(0);
			// Testing if the string starts with a / making it a closing tag
		} else if (braceEnclosedText.startsWith("/")) {
			name = substringAfter(braceEnclosedText, "/");
			this.tagOpenClose.add(1);
			this.hasAttribute.add(0);
		}
		return name;
	}


	@Override
	public List<String> validate(String xmlDocument) {

		/*
		 * Creating a List of strings for the final output, a new stack, a List of lines
		 * of the xml document and an ArrayList of XmlTag objects
		 */
		List<String> output = new ArrayList<String>();
		BasicXmlTagStack stack = new BasicXmlTagStack();
		List<String> xmlText = new ArrayList<String>(Arrays.asList(xmlDocument.split("\n")));
		ArrayList<XmlTag> xmlTags = findAllTags(xmlText);

		// Creating a for loop that loops as many times as there are tags
		for (int numTags = 0; numTags < xmlTags.size(); numTags++) {
			// Testing if the tag is an open tag
			if (tagOpenClose.get(numTags) == 0) {
				stack.push(xmlTags.get(numTags));
				// Testing if the opening tag has an attribute
				if (this.hasAttribute.get(numTags) == 1) {
					output.add(0, "Attribute not quoted");
					output.add(1, xmlTags.get(numTags).name);
					output.add(2, Integer.toString(xmlTags.get(numTags).index));
					output.add(3, this.attributeName);
					output.add(4, Integer.toString(xmlTags.get(numTags).index));
					return output;
				}
				// Else the tag is a closing tag
			} else {
				// Testing if the stack is empty
				if (stack.getCount() == 0) {
					output.add(0, "Orphan closing tag");
					output.add(1, xmlTags.get(numTags).name);
					output.add(2, Integer.toString(xmlTags.get(numTags).index));
					return output;
					/*
					 * Testing if the name of the first item on the stack is the same as the name of
					 * the current tag
					 */
				} else if (stack.peek(0).name.equals(xmlTags.get(numTags).name)) {
					stack.pop();
					// Else there is an opening tag on the stack but the closing tag is not the same
				} else {
					output.add(0, "Tag mismatch");
					output.add(1, stack.peek(0).name);
					output.add(2, Integer.toString(stack.peek(0).index));
					output.add(3, xmlTags.get(numTags).name);
					output.add(4, Integer.toString(xmlTags.get(numTags).index));
					return output;
				}
			}
		}

		// Testing if the stack is not empty
		if (stack.getCount() != 0) {
			output.add(0, "Unclosed tag at end");
			output.add(1, stack.peek(0).name);
			output.add(2, Integer.toString(stack.peek(0).index));
			return output;
			// Else the stack is empty
		} else {
			return null;
		}
	}
}
