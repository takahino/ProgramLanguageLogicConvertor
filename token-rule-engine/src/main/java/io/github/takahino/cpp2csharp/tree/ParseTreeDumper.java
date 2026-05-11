// === LICENSE_START ===
// # LICENSE
// 
// This software is licensed only under the T. Hino Commercial License
// (THCL) v1.0. Use, copying, modification, distribution, academic use,
// commercial use, and use by corporations or legal entities require
// compliance with the terms below.
// 
// ---
// 
// ## T. Hino Commercial License (THCL) v1.0
// 
// Copyright (c) 2026 T. Hino. All rights reserved.
// 
// This license governs the use of ProgramLanguageLogicConvertor
// (hereinafter "the Software"), developed by T. Hino (hereinafter "the Author").
// 
// 1. Grant of License
//    Any person or entity wishing to use, copy, modify, distribute, or
//    otherwise handle the Software must submit a usage application to the
//    Author and obtain written or electronic approval before a license is
//    granted.
//    Any use without such approval shall be deemed copyright infringement.
// 
//    Electronic records include:
//    - Email
//    - Comments made by the Author on the Software's repository
// 
// 2. License Term
//    The license is valid for one (1) year from the date of grant.
//    To continue use, a renewal application must be submitted to the Author
//    no later than thirty (30) days before expiration, and re-approval must
//    be obtained.
// 
// 3. License Fee
//    The license fee shall be determined separately by mutual agreement
//    between the Author and the licensee.
//    If the license is granted free of charge, such agreement shall be
//    explicitly stated in writing or electronic record.
//    The Author reserves the right to set a new license fee upon each renewal.
// 
// 4. Effect of License Expiration
//    If renewal is not approved, the license to use the Software itself
//    shall expire at the end of the license term.
//    However, any output or deliverables (e.g., converted source code)
//    generated using the Software during the valid license period may
//    continue to be used after license expiration.
// 
// 5. Restriction on Modification and Redistribution
//    Any modification or redistribution of the Software requires separate
//    written or electronic approval from the Author.
//    Use, distribution, or publication of modified versions without such
//    approval shall constitute a violation of this license.
// 
// 6. Retention of Copyright Notice
//    The following copyright notice must be retained in all copies and
//    derivative works of the Software:
// 
//    "Copyright (c) 2026 T. Hino. Licensed under THCL."
// 
//    The method of retention shall be as follows depending on usage:
// 
//    (a) When copying or modifying source code:
//        The above notice must be included in a comment at the top of
//        each source file.
// 
//    (b) When distributing in binary or executable form:
//        At least one of the following must be satisfied:
//        - Include the above notice in the application's About dialog
//        - Include the above notice in documentation (e.g., README)
//          bundled with the distribution
// 
//    (c) When used as an internal tool or system:
//        The above notice must be included in the help screen or
//        version information screen of the system.
// 
//    Modification or deletion of the above notice shall constitute
//    a violation of this license.
// 
// 7. Disclaimer
//    The Software is provided "as is" without warranty of any kind.
//    The Author shall not be liable for any damages arising from the
//    use of the Software.
// 
// 8. Citation Requirement for Academic Use
//    When the logic, algorithms, or design concepts of the Software are
//    used or referenced in papers, technical documents, academic presentations,
//    or similar works, the Author and the Software must be explicitly cited
//    in the following format:
// 
//    [Citation Format]
//    T. Hino, "ProgramLanguageLogicConvertor", GitHub,
//    https://github.com/takahino/ProgramLanguageLogicConvertor, [Date Accessed]
// 
//    Academic use without proper citation shall constitute a violation
//    of this license.
//    If a citation is made, it is recommended that the Author be notified
//    via email or a repository Issue.
// 
// ---
// 
// Contact  : takahino@ymail.ne.jp
// Inquiries: https://github.com/takahino/ProgramLanguageLogicConvertor/issues
// Repository: https://github.com/takahino/ProgramLanguageLogicConvertor
// 
// ---
// 
// ## Applicable License
// 
// All use cases are governed by THCL v1.0. A usage application and approval
// from the Author are required before use unless the Author has separately
// granted permission in writing or electronic record.
// === LICENSE_END ===

package io.github.takahino.cpp2csharp.tree;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

/**
 * ANTLR ParseTree の文字列表現をダンプするユーティリティ。
 *
 * <p>
 * ルール設計のデバッグ用。トークン文字列を中心に、木構造を可読な形式で出力する。 同一文字列がネストして繰り返す場合は圧縮表示する（例:
 * {@code expression [3]}）。
 * </p>
 *
 * <p>
 * ANTLR の {@link ParseTree} と {@link CommonTokenStream} を 直接使用する。
 * </p>
 */
public final class ParseTreeDumper {

	private static final String INDENT = "  ";

	/**
	 * 木の文字列表現を生成する。
	 *
	 * @param tree
	 *            ANTLR パースツリーのルート
	 * @param ruleNames
	 *            パーサーのルール名配列（{@code parser.getRuleNames()}）
	 * @param tokenStream
	 *            全トークンがロードされた CommonTokenStream
	 * @return ダンプ文字列
	 */
	public String dump(ParseTree tree, String[] ruleNames, CommonTokenStream tokenStream) {
		StringBuilder sb = new StringBuilder();
		sb.append("--- フラットトークン列 (パターンマッチ対象) ---\n");
		List<String> tokens = extractDefaultChannelTokens(tokenStream);
		sb.append(String.join(" ", tokens)).append("\n\n");
		sb.append("--- 木構造 (ネスト同一は圧縮) ---\n");
		dumpNode(tree, ruleNames, 0, new ArrayList<>(), false, sb);
		return sb.toString();
	}

	/**
	 * CommonTokenStream から DEFAULT_CHANNEL のトークン文字列を抽出する（EOF を除く）。
	 */
	private List<String> extractDefaultChannelTokens(CommonTokenStream tokenStream) {
		List<String> result = new ArrayList<>();
		for (Token token : tokenStream.getTokens()) {
			if (token.getChannel() == Token.DEFAULT_CHANNEL && token.getType() != Token.EOF) {
				result.add(token.getText());
			}
		}
		return result;
	}

	/**
	 * ParseTree ノードを再帰的にダンプする。
	 *
	 * @param node
	 *            現在のノード
	 * @param ruleNames
	 *            ルール名配列
	 * @param depth
	 *            現在の深さ
	 * @param ancestors
	 *            祖先のルール名リスト（ネスト圧縮用）
	 * @param skipOutput
	 *            このノード自身の出力をスキップするか
	 * @param sb
	 *            出力バッファ
	 */
	private void dumpNode(ParseTree node, String[] ruleNames, int depth, List<String> ancestors, boolean skipOutput,
			StringBuilder sb) {
		if (node instanceof TerminalNode terminal) {
			if (terminal.getSymbol().getType() != Token.EOF) {
				sb.append(INDENT.repeat(depth)).append(terminal.getText()).append("\n");
			}
			return;
		}

		if (!(node instanceof ParserRuleContext ruleCtx)) {
			return;
		}

		String name = ruleNames[ruleCtx.getRuleIndex()];
		String subtreeText = getSubtreeText(node);

		int childCount = node.getChildCount();
		List<String> newAncestors = new ArrayList<>(ancestors);
		newAncestors.add(name);

		// 同一テキスト連鎖: 子が1つかつ文字列が同一なら圧縮
		if (childCount == 1) {
			ParseTree onlyChild = node.getChild(0);
			String childText = getSubtreeText(onlyChild);
			if (subtreeText.equals(childText)) {
				int grandChildCount = onlyChild.getChildCount();
				if (grandChildCount == 1 && !childText.contains(" ")) {
					ParseTree grandChild = onlyChild.getChild(0);
					String grandChildText = getSubtreeText(grandChild);
					if (childText.equals(grandChildText)) {
						if (!skipOutput) {
							sb.append(INDENT.repeat(depth)).append(name).append(" : ").append(subtreeText).append("\n");
						}
						return;
					}
				}
				if (!skipOutput) {
					sb.append(INDENT.repeat(depth)).append(name).append(" : ").append(subtreeText).append("\n");
				}
				boolean childHasBranch = grandChildCount != 1;
				dumpNode(onlyChild, ruleNames, depth + 1, newAncestors, !childHasBranch, sb);
				return;
			}
		}

		// 同一ルール名のネストをカウント
		int run = 1;
		for (int i = ancestors.size() - 1; i >= 0; i--) {
			if (ancestors.get(i).equals(name)) {
				run++;
			} else {
				break;
			}
		}

		if (!skipOutput) {
			String line = name + (run > 1 ? " [" + run + "]" : "") + " : " + subtreeText;
			if (run > 1) {
				int firstInRun = depth - run + 1;
				sb.append(INDENT.repeat(Math.max(0, firstInRun))).append(line).append("\n");
			} else {
				sb.append(INDENT.repeat(depth)).append(line).append("\n");
			}
		}

		for (int i = 0; i < childCount; i++) {
			dumpNode(node.getChild(i), ruleNames, depth + 1, newAncestors, false, sb);
		}
	}

	/**
	 * ノードのサブツリーに含まれる全 TerminalNode のテキストをスペース区切りで返す（EOF を除く）。
	 */
	private String getSubtreeText(ParseTree node) {
		if (node instanceof TerminalNode terminal) {
			int type = terminal.getSymbol().getType();
			return type == Token.EOF ? "" : terminal.getText();
		}
		List<String> tokens = new ArrayList<>();
		collectTerminalTexts(node, tokens);
		return String.join(" ", tokens);
	}

	private void collectTerminalTexts(ParseTree node, List<String> result) {
		if (node instanceof TerminalNode terminal) {
			if (terminal.getSymbol().getType() != Token.EOF) {
				result.add(terminal.getText());
			}
			return;
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			collectTerminalTexts(node.getChild(i), result);
		}
	}
}
