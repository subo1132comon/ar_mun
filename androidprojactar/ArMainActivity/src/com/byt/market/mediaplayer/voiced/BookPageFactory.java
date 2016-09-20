package com.byt.market.mediaplayer.voiced;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Vector;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;

public class BookPageFactory {
	StringBuilder  word;
	private static final String TAG = "BookPageFactory";
	private File book_file = null;
	private int m_backColor = 0xffff9e85; // ������ɫ
	private Bitmap m_book_bg = null;
	private int m_fontSize = 20;
	private boolean m_isfirstPage, m_islastPage;
	private Vector<String> m_lines = new Vector<String>();
	private MappedByteBuffer m_mbBuf = null;// �ڴ��е�ͼ���ַ�
	private int m_mbBufBegin = 0;// ��ǰҳ��ʼλ��
	private int m_mbBufEnd = 0;// ��ǰҳ�յ�λ��

	private int m_mbBufLen = 0; // ͼ���ܳ���

	private String m_strCharsetName = "UTF-8";

	private int m_textColor = Color.rgb(28, 28, 28);

	private int marginHeight = 15; // �������Ե�ľ���
	private int marginWidth = 15; // �������Ե�ľ���
	private int mHeight;
	private int mLineCount; // ÿҳ������ʾ������
	private Paint mPaint;

	private float mVisibleHeight; // �������ݵĿ�
	private float mVisibleWidth; // �������ݵĿ�
	private int mWidth;

	public BookPageFactory(int w, int h) {
		mWidth = w;
		mHeight = h;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);// ����
		mPaint.setTextAlign(Align.LEFT);// ������
		mPaint.setTextSize(m_fontSize);// �����С
		mPaint.setColor(m_textColor);// ������ɫ
		mVisibleWidth = mWidth - marginWidth * 2;
		mVisibleHeight = mHeight - marginHeight * 2;
		mLineCount = (int) (mVisibleHeight / m_fontSize) - 1; // ����ʾ������,-1����Ϊ�ײ���ʾ���ȵ�λ�����ױ���ס
	}

	public int getM_fontSize() {
		return m_fontSize;
	}

	public int getmLineCount() {
		return mLineCount;
	}

	public boolean isfirstPage() {
		return m_isfirstPage;
	}

	public boolean islastPage() {
		return m_islastPage;
	}

	/**
	 * ���ҳ
	 * 
	 * @throws IOException
	 */
	public void nextPage() throws IOException {
		if (m_mbBufEnd >= m_mbBufLen) {
			m_islastPage = true;
			return;
		} else
			m_islastPage = false;
		m_lines.clear();
		m_mbBufBegin = m_mbBufEnd;// ��һҳҳ��ʼλ��=��ǰҳ����λ��
		m_lines = pageDown();
		
	}

	public void currentPage() throws IOException {
		m_lines.clear();
		m_lines = pageDown();
	}

	public void onDraw(Canvas c) {
		word=new StringBuilder();
		mPaint.setTextSize(m_fontSize);
		mPaint.setColor(m_textColor);
		if (m_lines.size() == 0)
			m_lines = pageDown();
		if (m_lines.size() > 0) {
			if (m_book_bg == null)
				c.drawColor(m_backColor);
			else
				c.drawBitmap(m_book_bg, 0, 0, null);
			int y = marginHeight;
			for (String strLine : m_lines) {
				y += m_fontSize;
				c.drawText(strLine, marginWidth, y, mPaint);
				word.append(strLine);
			}
			Read.words=word.toString();
			word=null;
		}
		float fPercent = (float) (m_mbBufBegin * 1.0 / m_mbBufLen);
		DecimalFormat df = new DecimalFormat("#0.0");
		String strPercent = df.format(fPercent * 100) + "%";
		int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;
		c.drawText(strPercent, mWidth - nPercentWidth, mHeight - 5, mPaint);
	}

	/**
	 * 
	 * @param strFilePath
	 * @param begin
	 *            ��ʾ��ǩ��¼��λ�ã���ȡ��ǩʱ����beginֵ��m_mbBufEnd���ڶ�ȡnextpage�����ɹ���ȡ������ǩ
	 *            ��¼ʱ��m_mbBufBegin��ʼλ����Ϊ��ǩ��¼
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public void openbook(String strFilePath, int begin) throws IOException {
		book_file = new File(strFilePath);
		long lLen = book_file.length();
		m_mbBufLen = (int) lLen;
		m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, lLen);
		Log.d(TAG, "total lenth��" + m_mbBufLen);
		// �����Ѷ�����
		if (begin >= 0) {
			m_mbBufBegin = begin;
			m_mbBufEnd = begin;
		} else {
		}
	}

	/**
	 * ��ָ��ҳ����һҳ
	 * 
	 * @return ��һҳ������ Vector<String>
	 */
	protected Vector<String> pageDown() {
		mPaint.setTextSize(m_fontSize);
		mPaint.setColor(m_textColor);
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
			byte[] paraBuf = readParagraphForward(m_mbBufEnd);
			m_mbBufEnd += paraBuf.length;// ÿ�ζ�ȡ�󣬼�¼������λ�ã���λ���Ƕ������λ��
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);// ת�����ƶ�GBK����
			} catch (UnsupportedEncodingException e) {
				Log.d("logcart", "pageUp->ת������ʧ��-----------------"+e);
				Log.e(TAG, "pageDown->ת������ʧ��", e);
			}
			String strReturn = "";
			// �滻���س����з�
			if (strParagraph.indexOf("\r\n") != -1) {
				strReturn = "\r\n";
				strParagraph = strParagraph.replaceAll("\r\n", "");
			} else if (strParagraph.indexOf("\n") != -1) {
				strReturn = "\n";
				strParagraph = strParagraph.replaceAll("\n", "");
			}

			if (strParagraph.length() == 0) {
				lines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				// ��һ������
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				
				if(nSize>strParagraph.length()){
					nSize = strParagraph.length();
				}
				lines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);// �õ�ʣ�������
				// ��������������ٻ�
				if (lines.size() >= mLineCount) {
					break;
				}
			}
			// �����ҳ���һ��ֻ��ʾ��һ���֣�����¶�λ������λ��
			if (strParagraph.length() != 0) {
				try {
					m_mbBufEnd -= (strParagraph + strReturn)
							.getBytes(m_strCharsetName).length;
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "pageDown->��¼������λ��ʧ��", e);
				}
			}
		}
		return lines;
	}

	/**
	 * �õ�����ҳ�Ľ���λ��
	 */
	protected void pageUp() {
		if (m_mbBufBegin < 0)
			m_mbBufBegin = 0;
		Vector<String> lines = new Vector<String>();
		String strParagraph = "";
		while (lines.size() < mLineCount && m_mbBufBegin > 0) {
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(m_mbBufBegin);
			m_mbBufBegin -= paraBuf.length;// ÿ�ζ�ȡһ�κ�,��¼��ʼ��λ��,�Ƕ��׿�ʼ��λ��
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "pageUp->ת������ʧ��", e);
				Log.d("logcart", "pageUp->ת������ʧ��-----------------"+e);
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			strParagraph = strParagraph.replaceAll("\n", "");
			// ����ǿհ��У�ֱ�����
			if (strParagraph.length() == 0) {
				paraLines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				// ��һ������
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth,
						null);
				if(nSize>strParagraph.length()){
					nSize = strParagraph.length();
				}
				paraLines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
			}
			lines.addAll(0, paraLines);
		}

		while (lines.size() > mLineCount) {
			try {
				m_mbBufBegin += lines.get(0).getBytes(m_strCharsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "pageUp->��¼��ʼ��λ��ʧ��", e);
			}
		}
		m_mbBufEnd = m_mbBufBegin;// ����һҳ�Ľ����������һҳ����ʼ��
		return;
	}

	/**
	 * ��ǰ��ҳ
	 * 
	 * @throws IOException
	 */
	protected void prePage() throws IOException {
		if (m_mbBufBegin <= 0) {
			m_mbBufBegin = 0;
			m_isfirstPage = true;
			return;
		} else
			m_isfirstPage = false;
		m_lines.clear();
		pageUp();
		m_lines = pageDown();
	}

	/**
	 * ��ȡָ��λ�õ���һ������
	 * 
	 * @param nFromPos
	 * @return byte[]
	 */
	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;
		int i;
		byte b0, b1;
		if (m_strCharsetName.equals("UTF-16LE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}

		} else if (m_strCharsetName.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else {
			i = nEnd - 1;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {// 0x0a��ʾ���з�
					i++;
					break;
				}
				i--;
			}
		}
		if (i < 0)
			i = 0;
		int nParaSize = nEnd - i;
		int j;
		byte[] buf = new byte[nParaSize];
		for (j = 0; j < nParaSize; j++) {
			buf[j] = m_mbBuf.get(i + j);
		}
		return buf;
	}

	/**
	 * ��ȡָ��λ�õ���һ������
	 * 
	 * @param nFromPos
	 * @return byte[]
	 */
	protected byte[] readParagraphForward(int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		// ���ݱ����ʽ�жϻ���
		if (m_strCharsetName.equals("UTF-16LE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (m_strCharsetName.equals("UTF-16BE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x00 && b1 == 0x0a) {
					break;
				}
			}
		} else {
			while (i < m_mbBufLen) {
				b0 = m_mbBuf.get(i++);
				if (b0 == 0x0a) {
					break;
				}
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = m_mbBuf.get(nFromPos + i);
		}
		return buf;
	}

	public void setBgBitmap(Bitmap BG) {
		m_book_bg = BG;
	}

	public void setM_fontSize(int m_fontSize) {
		this.m_fontSize = m_fontSize;
		mLineCount = (int) (mVisibleHeight / m_fontSize) - 1;
	}

	// ����ҳ����ʼ��
	public void setM_mbBufBegin(int m_mbBufBegin) {
		this.m_mbBufBegin = m_mbBufBegin;
	}

	// ����ҳ�������
	public void setM_mbBufEnd(int m_mbBufEnd) {
		this.m_mbBufEnd = m_mbBufEnd;
	}

	public int getM_mbBufBegin() {
		return m_mbBufBegin;
	}

	public String getFirstLineText() {
		return m_lines.size() > 0 ? m_lines.get(0) : "";
	}

	public int getM_textColor() {
		return m_textColor;
	}

	public void setM_textColor(int m_textColor) {
		this.m_textColor = m_textColor;
	}

	public int getM_mbBufLen() {
		return m_mbBufLen;
	}

	public int getM_mbBufEnd() {
		return m_mbBufEnd;
	}

}
