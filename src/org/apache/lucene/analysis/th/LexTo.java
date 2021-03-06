package org.apache.lucene.analysis.th;

import java.io.*;
import java.util.*;

public class LexTo {

	// Private variables
	private VTrie dict; // For storing words from dictionary
	private ParseTree ptree; // Parsing tree (for Thai words)

	// Returned variables
	private Vector indexList; // List of word index positions
	private Vector lineList; // List of line index positions
	private Vector typeList; // List of word types (for word only)
	private Iterator iter; // Iterator for indexList OR lineList (depends on the
							// call)

	/*******************************************************************/
	/*********************** Return index list *************************/
	/*******************************************************************/
	public Vector getIndexList() {
		return indexList;
	}

	/*******************************************************************/
	/*********************** Return type list *************************/
	/*******************************************************************/
	public Vector getTypeList() {
		return typeList;
	}

	/*******************************************************************/
	/******************** Iterator for index list **********************/
	/*******************************************************************/
	// Return iterator's hasNext for index list
	public boolean hasNext() {
		if (!iter.hasNext())
			return false;
		return true;
	}

	// Return iterator's first index
	public int first() {
		return 0;
	}

	// Return iterator's next index
	public int next() {
		return ((Integer) iter.next()).intValue();
	}

	/*******************************************************************/
	/************** Constructor (passing dictionary file ) *************/
	/*******************************************************************/

	public LexTo(Set<String> lexitron_utf) throws IOException {

		dict = new VTrie();
		addDomainDict(lexitron_utf);

		indexList = new Vector();
		lineList = new Vector();
		typeList = new Vector();
		ptree = new ParseTree(dict, indexList, typeList);

	} // Constructor

	/*******************************************************************/
	/************************ addDomainDict ****************************/
	/*******************************************************************/
	public void addDomainDict(Set<String> lexitron) {

		// Read words from dictionary
		String line;

		Iterator<String> e = lexitron.iterator();
		while (e.hasNext()) {
			line = e.next();
			line = line.trim();
			if (line.length() > 0)
				dict.add(line.trim(), 1);
		}

	} // addDomainDict

	/****************************************************************/
	/************************** wordInstance ************************/
	/****************************************************************/
	public void wordInstance(String text) {

		if (indexList != null)
			indexList.clear();
		if (typeList != null)
			typeList.clear();
		int pos, index;
		String word;
		boolean found;
		char ch;

		pos = 0;
		while (pos < text.length()) {

			// Check for special characters and English words/numbers
			ch = text.charAt(pos);

			// English
			if (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z'))) {
				while ((pos < text.length())
						&& (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z'))))
					ch = text.charAt(pos++);
				if (pos < text.length())
					pos--;
				indexList.addElement(new Integer(pos));
				typeList.addElement(new Integer(3));
			}
			// Digits
			else if (((ch >= '0') && (ch <= '9'))
					|| ((ch >= '๐') && (ch <= '๙'))) {
				while ((pos < text.length())
						&& (((ch >= '0') && (ch <= '9'))
								|| ((ch >= '๐') && (ch <= '๙')) || (ch == ',') || (ch == '.')))
					ch = text.charAt(pos++);
				if (pos < text.length())
					pos--;
				indexList.addElement(new Integer(pos));
				typeList.addElement(new Integer(3));
			}
			// Special characters
			else if ((ch <= '~') || (ch == 'ๆ') || (ch == 'ฯ') || (ch == '“')
					|| (ch == '”') || (ch == ',')) {
				pos++;
				indexList.addElement(new Integer(pos));
				typeList.addElement(new Integer(4));
			}
			// Thai word (known/unknown/ambiguous)
			else
				pos = ptree.parseWordInstance(pos, text);
		} // While all text length
		iter = indexList.iterator();
	} // wordInstance

	/****************************************************************/
	/************************** lineInstance ************************/
	/****************************************************************/
	public void lineInstance(String text) {

		int windowSize = 10; // for detecting parentheses, quotes
		int curType, nextType, tempType, curIndex, nextIndex, tempIndex;
		lineList.clear();
		wordInstance(text);
		int i;
		for (i = 0; i < typeList.size() - 1; i++) {
			curType = ((Integer) typeList.elementAt(i)).intValue();
			curIndex = ((Integer) indexList.elementAt(i)).intValue();

			if ((curType == 3) || (curType == 4)) {
				// Parenthesese
				if ((curType == 4) && (text.charAt(curIndex - 1) == '(')) {
					int pos = i + 1;
					while ((pos < typeList.size()) && (pos < i + windowSize)) {
						tempType = ((Integer) typeList.elementAt(pos))
								.intValue();
						tempIndex = ((Integer) indexList.elementAt(pos++))
								.intValue();
						if ((tempType == 4)
								&& (text.charAt(tempIndex - 1) == ')')) {
							lineList.addElement(new Integer(tempIndex));
							i = pos - 1;
							break;
						}
					}
				}
				// Single quote
				else if ((curType == 4) && (text.charAt(curIndex - 1) == '\'')) {
					int pos = i + 1;
					while ((pos < typeList.size()) && (pos < i + windowSize)) {
						tempType = ((Integer) typeList.elementAt(pos))
								.intValue();
						tempIndex = ((Integer) indexList.elementAt(pos++))
								.intValue();
						if ((tempType == 4)
								&& (text.charAt(tempIndex - 1) == '\'')) {
							lineList.addElement(new Integer(tempIndex));
							i = pos - 1;
							break;
						}
					}
				}
				// Double quote
				else if ((curType == 4) && (text.charAt(curIndex - 1) == '\"')) {
					int pos = i + 1;
					while ((pos < typeList.size()) && (pos < i + windowSize)) {
						tempType = ((Integer) typeList.elementAt(pos))
								.intValue();
						tempIndex = ((Integer) indexList.elementAt(pos++))
								.intValue();
						if ((tempType == 4)
								&& (text.charAt(tempIndex - 1) == '\"')) {
							lineList.addElement(new Integer(tempIndex));
							i = pos - 1;
							break;
						}
					}
				} else
					lineList.addElement(new Integer(curIndex));
			} else {
				nextType = ((Integer) typeList.elementAt(i + 1)).intValue();
				nextIndex = ((Integer) indexList.elementAt(i + 1)).intValue();
				if ((nextType == 3)
						|| ((nextType == 4) && ((text.charAt(nextIndex - 1) == ' ')
								|| (text.charAt(nextIndex - 1) == '\"')
								|| (text.charAt(nextIndex - 1) == '(') || (text
								.charAt(nextIndex - 1) == '\''))))
					lineList.addElement(new Integer(((Integer) indexList
							.elementAt(i)).intValue()));
				else if ((curType == 1) && (nextType != 0) && (nextType != 4))
					lineList.addElement(new Integer(((Integer) indexList
							.elementAt(i)).intValue()));
			}
		}
		if (i < typeList.size())
			lineList.addElement(new Integer(((Integer) indexList
					.elementAt(indexList.size() - 1)).intValue()));
		iter = lineList.iterator();
	} // lineInstance

	/**
	 * Job's edited
	 * 
	 * @param line
	 * @return
	 */
	public float match(String line) {
		int begin, end, type;
		int unknown = 0, known = 0, ambiguous = 0, special = 0;
		// String word = null;
		Vector typeList = null;
		float thaiPercent;

		line = line.trim();
		if (line.length() > 0) {
			wordInstance(line);
			typeList = getTypeList();
			begin = first();
			int i = 0;
			while (hasNext()) {
				end = next();
				type = ((Integer) typeList.elementAt(i++)).intValue();
				// word = line.substring(begin, end);
				if (type == 0) {
					unknown++;
				} else if (type == 1) {
					known++;
				} else if (type == 2) {
					ambiguous++;
				} else {
					// TODO: Do something;
					special++;
				}
				begin = end;
			}
		}
		thaiPercent = ((float) (known + ambiguous) / (float) (known + unknown
				+ ambiguous + special));

		if (Float.isNaN(thaiPercent))
			thaiPercent = 0f;
		return thaiPercent;
	}

	public static void main(String[] args) throws IOException {
		LangChecker.createEnviroment();
		LexTo l = new LexTo(LangChecker.lexitron);

		System.out.println(l.match("ทดสอบ Test"));
	}

}
