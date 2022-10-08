package xmlvalidator;

import static java.lang.System.*;

public class BasicXmlTagStack implements XmlTagStack {

	private XmlTag[] stack = new XmlTag[0];
	private int itemCount = 0;

	@Override
	public void push(XmlTag item) {
		if (itemCount == stack.length) {
			int newLength = stack.length + 1;
			var tempArray = new XmlTag[newLength];
			arraycopy(stack, 0, tempArray, 0, stack.length);
			stack = tempArray;
		}
		stack[itemCount++] = item;
		// TODO Auto-generated method stub

	}


	@Override
	public XmlTag pop() {
		if (itemCount == 0)
			return null;
		else
			return stack[--itemCount];
	}


	@Override
	public XmlTag peek(int position) {
		if ((position > itemCount - 1) || (position < 0))
			return null;
		else
			return stack[itemCount - position - 1];
	}


	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return itemCount;
	}

}
