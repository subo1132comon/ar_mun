package com.byt.market.mediaplayer.txtreader;
//package com.codemany.txtreader;
//
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//
//
//
//public class ImmutableInverseTrieTest {
//
//	@Test
//	public void testDeserialize() {
//		// TODO test invalid format case
//
//		// reflective test
//		String test1 = "t\nao\nr\nr\nm\n*\n*";
//		ImmutableInverseTrie ans = ImmutableInverseTrie.deserialize(test1);
//		assertEquals(test1, ans.serialize());
//
//		// empty string
//		String test2 = "";
//		ans = ImmutableInverseTrie.deserialize(test2);
//		assertEquals(test2, ans.serialize());
//
//		// null
//		try {
//			ImmutableInverseTrie.deserialize((InputStream)null);
//			fail();
//		} catch (Exception e) {
//			assertTrue(true);
//		}
//	}
//
//	@Test
//	public void testFromWordList() {
//		String[] wordList = new String[] { "tor", "tarm", "term", "ton",
//				"hello" };
//		ImmutableInverseTrie ans = ImmutableInverseTrie.fromWordList2(wordList);
//		System.out.println(ans);
//		System.out.println("=======");
//		System.out.println(ans.toWordList());
//	}
//
//	@Test
//	public void testToWordList() throws IOException {
//		String file = FileUtil.readPlainText(new File("lexitron.txt"),
//				FileUtil.ms874);
//		FileUtil.writePlainText(new File("lexitron2.txt"), ImmutableInverseTrie
//				.fromWordList(file).serialize(), FileUtil.ms874);
//		FileUtil.writePlainText(new File("lexitron3.txt"), ImmutableInverseTrie
//				.fromWordList(file).toWordList(), FileUtil.ms874);
//	}
//
//	@Test
//	public void testLongestMatch() {
//		String[] wordList = new String[] { "tor", "tarm", "term", "ton",
//				"hello","หนุ่�?" };
//		ImmutableInverseTrie ans = ImmutableInverseTrie.fromWordList2(wordList);
//		String test = "tortarmtermtonnoหนุ่�?";
//		System.out.println("======================");
//		for (int i = 0; i <= test.length(); i++) {
//			System.out.println(i);
//			int pos = ans.longestMatch(test, i);
//			if (pos != -1)
//				System.out.println(new StringBuilder(test).insert(pos, '_'));
//		}
//	}
//
//	@Test
//	public void realWorldTest() {
//		try {
//			ImmutableInverseTrie ans = ImmutableInverseTrie
//					.deserialize(FileUtil.readPlainText(new File(
//							"trie_cache.txt"), FileUtil.ms874));
//			String test = "ทยอยส่งความคิด�?ห็นจากผู้ทดลองใช้ครั�?  โดยผมจะรวบรวมมาสรุปอีกครั้งครับ\r\n"
//					+ "ต้องขอบคุณอุ้ย ที่ส่งมา�?ป็นคนแรก�?ลย\r\n"
//					+ " \r\n"
//					+ "สำหรับคนอื่น�? ขอร้องให้ช่วยสรุปให้กับทาง �?.จอ�? ด้วยครับ ยิ่งได้ความคิด�?ห็นเร็�? ยิ่งทำให้ออกมาสมบูรณ์\r\n"
//					+ "และรวด�?ร็วขึ้น\r\n"
//					+ " \r\n"
//					+ "ขอบคุณครับ\r\n"
//					+ "หนุ่�?";
//			for (int i = 0; i <= test.length(); i++) {
//				System.out.println(i);
//				int pos = ans.longestMatch(test, i);
//				if (pos != -1)
//					System.out
//							.println(new StringBuilder(test).insert(pos, '_'));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
