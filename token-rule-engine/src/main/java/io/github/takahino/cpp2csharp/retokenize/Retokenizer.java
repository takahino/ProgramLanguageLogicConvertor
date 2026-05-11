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

package io.github.takahino.cpp2csharp.retokenize;

import io.github.takahino.cpp2csharp.rule.CollectingErrorListener;
import io.github.takahino.cpp2csharp.rule.LanguageLexerFactory;
import io.github.takahino.cpp2csharp.tree.AstNode;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * トークンノード列を再トークン化するクラス。
 *
 * <p>
 * 変換フェーズ間（pre→main、main→post 等）に使用し、置換由来の合成トークンを 個別の C++ トークンに分解し直す。
 * </p>
 *
 * <p>
 * 再トークン化により、後続フェーズのパターンマッチングが正しく動作するようになる。
 * </p>
 */
public class Retokenizer {

	private static final Logger LOGGER = LoggerFactory.getLogger(Retokenizer.class);

	private final LanguageLexerFactory lexerFactory;

	/**
	 * デフォルトコンストラクタ。lexerFactory が null の場合、retokenize() で例外がスローされる。
	 */
	public Retokenizer() {
		this(null);
	}

	/**
	 * コンストラクタ。LanguageLexerFactory を注入する。
	 *
	 * @param lexerFactory
	 *            言語固有の Lexer を生成するファクトリ
	 */
	public Retokenizer(LanguageLexerFactory lexerFactory) {
		this.lexerFactory = lexerFactory;
	}

	/**
	 * トークンノード列を再トークン化し、新トークンストリームに対応するコメントマップも返す。
	 *
	 * <p>
	 * commentsBeforeToken が空でない場合、コメント・改行・#include 等をソースに含めて構築し、
	 * 再トークン化後も保持する。空の場合はスペース区切りで結合する（テスト用）。
	 * </p>
	 *
	 * @param tokenNodes
	 *            再トークン化対象のトークンノード列
	 * @param commentsBeforeToken
	 *            各トークン直前のコメント・改行・空白のマップ（空可）
	 * @return 再トークン化されたノード列とコメントマップ
	 */
	public RetokenizeResult retokenize(List<AstNode> tokenNodes, Map<Integer, List<String>> commentsBeforeToken) {
		String source = buildSource(tokenNodes, commentsBeforeToken);
		LOGGER.debug("再トークン化: {} chars", source.length());
		return lex(source);
	}

	private String buildSource(List<AstNode> tokenNodes, Map<Integer, List<String>> commentsBeforeToken) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < tokenNodes.size()) {
			AstNode node = tokenNodes.get(i);
			int streamIdx = node.getStreamIndex();
			int groupEnd = i + 1;
			if (node.getStreamIndex() < 0) {
				while (groupEnd < tokenNodes.size() && tokenNodes.get(groupEnd).getStreamIndex() < 0) {
					groupEnd++;
				}
			}
			StringBuilder groupText = new StringBuilder();
			for (int j = i; j < groupEnd; j++) {
				groupText.append(tokenNodes.get(j).getText());
			}
			String gText = groupText.toString();
			if ("<EOF>".equals(gText)) {
				i = groupEnd;
				continue;
			}
			// コメント・改行・#include を保持: commentsBeforeToken から取得して出力
			if (streamIdx >= 0 && commentsBeforeToken != null && !commentsBeforeToken.isEmpty()) {
				List<String> comments = commentsBeforeToken.get(streamIdx);
				if (comments != null) {
					for (String item : comments) {
						sb.append(item);
					}
				}
			} else if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(gText);
			i = groupEnd;
		}
		return sb.toString();
	}

	private RetokenizeResult lex(String source) {
		if (lexerFactory == null) {
			throw new IllegalStateException(
					"LanguageLexerFactory が設定されていません。Retokenizer(LanguageLexerFactory) コンストラクタを使用してください。");
		}
		Lexer lexer = lexerFactory.createLexer(CharStreams.fromString(source));
		CollectingErrorListener errorListener = new CollectingErrorListener();
		lexer.removeErrorListeners();
		lexer.addErrorListener(errorListener);

		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		tokenStream.fill();

		if (errorListener.hasErrors()) {
			LOGGER.warn("再トークン化で字句エラー: {}", errorListener.getErrors());
		}

		Map<Integer, List<String>> commentsBeforeToken = buildCommentsMap(tokenStream);

		List<AstNode> result = new ArrayList<>();
		for (Token token : tokenStream.getTokens()) {
			if (token.getChannel() != Token.DEFAULT_CHANNEL)
				continue;
			if (token.getType() == Token.EOF)
				break;
			result.add(AstNode.tokenNode(token.getText(), token.getLine(), token.getCharPositionInLine(),
					token.getTokenIndex()));
		}
		return new RetokenizeResult(result, commentsBeforeToken);
	}

	public static Map<Integer, List<String>> buildCommentsMap(CommonTokenStream tokenStream) {
		Map<Integer, List<String>> result = new LinkedHashMap<>();
		List<String> pending = new ArrayList<>();

		for (Token token : tokenStream.getTokens()) {
			String text = token.getText();
			if (token.getChannel() == Token.HIDDEN_CHANNEL) {
				if (text.startsWith("//") || text.startsWith("/*")) {
					pending.add(text);
				} else if (text.startsWith("#")) {
					pending.add(text);
				} else if (isNewlineToken(text)) {
					pending.add(text);
				} else if (isWhitespaceToken(text)) {
					pending.add(text);
				}
			} else if (token.getChannel() == Token.DEFAULT_CHANNEL) {
				if (!pending.isEmpty()) {
					result.put(token.getTokenIndex(), new ArrayList<>(pending));
					pending.clear();
				}
			}
		}
		return result;
	}

	private static boolean isNewlineToken(String text) {
		return "\n".equals(text) || "\r\n".equals(text) || "\r".equals(text);
	}

	private static boolean isWhitespaceToken(String text) {
		if (text == null || text.isEmpty())
			return false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c != ' ' && c != '\t')
				return false;
		}
		return true;
	}
}
