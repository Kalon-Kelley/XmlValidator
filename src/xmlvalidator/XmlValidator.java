package xmlvalidator;

import java.util.*;

public interface XmlValidator {

	/**
	 * Uses an XmlTagStack to validate the nesting of XML elements in xmlDocument.
	 * elements in xmlDocument. Extra credit will be awarded if unquoted attribute
	 * values are found. This method returns when the first problem is found.
	 * 
	 * @param xmlDocument
	 *            The XML document as a String.
	 * 
	 * @return If the xmlDocument's elements are properly nested, null is returned.
	 *             Otherwise, a List<String> containing error information is
	 *             returned. The array contents depend on the type of problem that
	 *             occurs (see below):
	 * 
	 *             <pre>
	 *         If the stack is empty when a closing tag is found, the return List should be:
	 * 
	 *         [0] = "Orphan closing tag" 
	 *         [1] = Closing tag name 
	 *         [2] = Line # of closing tag name 
	 *              e.g. 
	 *         [0] = "Orphan closing tag"
	 *         [1] = "sometag" 
	 *         [2] = "35"
	 * 
	 *         If the stack is not empty when the end of the xmlDocument is reached:
	 * 
	 *         [0] = "Unclosed tag at end" 
	 *         [1] = Tag name from the top of the stack 
	 *         [2] = Line # of the tag name 
	 *              e.g. 
	 *         [0] = "Unclosed tag at end" 
	 *         [1] = "sometag" 
	 *         [2] = "14"
	 * 
	 *         If a closing tag name does not match the tag name on the top of the stack:
	 * 
	 *         [0] = "Tag mismatch" 
	 *         [1] = Tag name from the top of the stack 
	 *         [2] = Line # of the tag name 
	 *         [3] = Closing tag name 
	 *         [4] = Line # of the closing tag name 
	 *              e.g. 
	 *         [0] = "Tag mismatch" 
	 *         [1] = "sometag" 
	 *         [2] = "14" 
	 *         [3] = "someothertag" 
	 *         [4] = "30"
	 *         
	 *         EXTRA CREDIT:
	 *         [0] = "Attribute not quoted" 
	 *         [1] = Name of the tag that has an unquoted attribute
	 *         [2] = Line # of the tag name 
	 *         [3] = Attribute name 
	 *         [4] = Line # of the unquoted attribute value 
	 *              e.g. 
	 *         [0] = "Attribute not quoted" 
	 *         [1] = "sometag" 
	 *         [2] = "14" 
	 *         [3] = "someattribute" 
	 *         [4] = "15"
	 * 
	 *             </pre>
	 */
	public List<String> validate(String xmlDocument);

}
