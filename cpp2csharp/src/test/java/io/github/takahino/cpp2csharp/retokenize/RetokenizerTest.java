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

import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.tree.AstNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link Retokenizer} のユニットテスト。
 */
@DisplayName("Retokenizer テスト")
class RetokenizerTest {

	private final Retokenizer retokenizer = new Retokenizer(CppParserFactory.asLexerFactory());

	@Test
	@DisplayName("単純なトークン列を再トークン化できる")
	void testSimpleRetokenize() {
		// Create token nodes from scratch
		List<AstNode> nodes = List.of(AstNode.tokenNode("int", 1, 0, 0), AstNode.tokenNode("x", 1, 4, 1),
				AstNode.tokenNode("=", 1, 6, 2), AstNode.tokenNode("42", 1, 8, 3), AstNode.tokenNode(";", 1, 10, 4));

		List<AstNode> result = retokenizer.retokenize(nodes, Map.of()).tokenNodes();

		// Should produce the same tokens
		assertThat(result).isNotEmpty();
		List<String> texts = result.stream().map(AstNode::getText).toList();
		assertThat(texts).containsExactly("int", "x", "=", "42", ";");
	}

	@Test
	@DisplayName("合成置換トークンを再トークン化して個別トークンに分解できる")
	void testRetokenizeReplacementToken() {
		// A replacement token (streamIndex = -1) that contains multiple C++ tokens
		// concatenated
		// For example, a replacement node "Math.Sin(x)" should be split into tokens
		List<AstNode> nodes = List.of(AstNode.tokenNodeWithId("Math.Sin(x)", 1, 0, 100, -1));

		List<AstNode> result = retokenizer.retokenize(nodes, Map.of()).tokenNodes();

		// Should be split into individual tokens
		assertThat(result).isNotEmpty();
		List<String> texts = result.stream().map(AstNode::getText).toList();
		// Math.Sin(x) contains: Math, ., Sin, (, x, )
		assertThat(texts).containsExactly("Math", ".", "Sin", "(", "x", ")");
	}

	@Test
	@DisplayName("空のトークン列を再トークン化すると空のリストになる")
	void testEmptyTokenList() {
		List<AstNode> nodes = List.of();
		List<AstNode> result = retokenizer.retokenize(nodes, Map.of()).tokenNodes();
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("再トークン化後のノードのストリームインデックスは新しい字句解析インデックス")
	void testStreamIndexAfterRetokenize() {
		List<AstNode> nodes = List.of(AstNode.tokenNode("int", 1, 0, 0), AstNode.tokenNode("x", 1, 4, 1));

		List<AstNode> result = retokenizer.retokenize(nodes, Map.of()).tokenNodes();

		// All retokenized nodes should have non-negative streamIndex
		for (AstNode node : result) {
			assertThat(node.getStreamIndex()).isGreaterThanOrEqualTo(0);
		}
	}

	@Test
	@DisplayName("EOF トークンは結果に含まれない")
	void testNoEofToken() {
		List<AstNode> nodes = List.of(AstNode.tokenNode("x", 1, 0, 0));

		List<AstNode> result = retokenizer.retokenize(nodes, Map.of()).tokenNodes();

		assertThat(result).noneMatch(n -> "<EOF>".equals(n.getText()));
	}

	@Test
	@DisplayName("複数の置換トークンをスペース区切りで結合して再トークン化する")
	void testMultipleReplacementTokens() {
		// Multiple replacement tokens (streamIndex = -1)
		List<AstNode> nodes = List.of(AstNode.tokenNodeWithId("MessageBox", 1, 0, 1, -1),
				AstNode.tokenNodeWithId(".", 1, 0, 2, -1), AstNode.tokenNodeWithId("Show", 1, 0, 3, -1));

		List<AstNode> result = retokenizer.retokenize(nodes, Map.of()).tokenNodes();
		assertThat(result).isNotEmpty();
		List<String> texts = result.stream().map(AstNode::getText).toList();
		// All replacement tokens concatenated → "MessageBox.Show" → retokenized
		assertThat(texts).containsExactly("MessageBox", ".", "Show");
	}
}
