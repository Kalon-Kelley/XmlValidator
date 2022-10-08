package xmlvalidator;

public interface XmlTagStack {

	/**
	 * Pushes the given item onto the top of the stack.
	 * 
	 * @param item
	 */
	public void push(XmlTag item);


	/**
	 * Removes the top item from the stack.
	 * 
	 * @return The removed item. If the stack is empty when this method is called,
	 *         null is returned.
	 */
	public XmlTag pop();


	/**
	 * Returns, but does not remove, the item at the given position. 0 is the top, 1
	 * is the second item, and so on.
	 * 
	 * @param position
	 * 
	 * @return The item at the given position. If the stack is empty when this
	 *         method is called, or position is greater than count -1, null is
	 *         returned.
	 */
	public XmlTag peek(int position);


	/**
	 * @return The number of items on the stack
	 */
	public int getCount();

}
