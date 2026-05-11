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

package io.github.takahino.cpp2csharp.multi;

import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.mrule.MultiReplaceRule;
import io.github.takahino.cpp2csharp.mrule.MultiReplaceRuleLoader;
import io.github.takahino.cpp2csharp.tree.AstNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link MultiReplaceMatcher} のユニットテスト。
 */
@DisplayName("MultiReplaceMatcher テスト")
class MultiReplaceMatcherTest {

	private final MultiReplaceMatcher matcher = new MultiReplaceMatcher();
	private final MultiReplaceRuleLoader loader = new MultiReplaceRuleLoader(CppParserFactory.asLexerFactory());

	/** トークン文字列リストから AstNode リストを生成するヘルパー */
	private List<AstNode> nodes(String... tokens) {
		List<AstNode> result = new java.util.ArrayList<>();
		for (int i = 0; i < tokens.length; i++) {
			result.add(AstNode.tokenNode(tokens[i], 1, i, i));
		}
		return result;
	}

	@Test
	@DisplayName("単純な1つの find spec にマッチする")
	void testSingleFindSpec() {
		String content = """
				find: BOOL
				replace: bool
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		List<AstNode> tokenNodes = nodes("int", "x", ";", "BOOL", "y", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).stepMatches()).hasSize(1);
		assertThat(results.get(0).stepMatches().get(0).getStartIndex()).isEqualTo(3);
	}

	@Test
	@DisplayName("連続する2つの find spec にマッチする")
	void testConsecutiveSpecs() {
		String content = """
				find: BOOL
				replace: bool
				find: x
				replace: y
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// "BOOL x" at positions 0-1 should match
		List<AstNode> tokenNodes = nodes("BOOL", "x", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).hasSize(1);
		MultiReplaceMatchResult result = results.get(0);
		assertThat(result.stepMatches()).hasSize(2);
		assertThat(result.stepMatches().get(0).getStartIndex()).isEqualTo(0);
		assertThat(result.stepMatches().get(1).getStartIndex()).isEqualTo(1);
	}

	@Test
	@DisplayName("連続する2つの find spec が連続していない場合はマッチしない")
	void testConsecutiveSpecsNoMatch() {
		String content = """
				find: BOOL
				replace: bool
				find: z
				replace: y
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// "BOOL" at 0, "z" at 2 → not consecutive
		List<AstNode> tokenNodes = nodes("BOOL", "x", "z", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("skip: を使ったスキップマッチ")
	void testSkipMatch() {
		String content = """
				find: BOOL
				replace: bool
				skip:
				find: x
				replace: y
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// "BOOL" at 0, "x" at 2 (with skip)
		List<AstNode> tokenNodes = nodes("BOOL", "z", "x", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).hasSize(1);
		MultiReplaceMatchResult result = results.get(0);
		assertThat(result.stepMatches()).hasSize(2);
		assertThat(result.stepMatches().get(0).getStartIndex()).isEqualTo(0);
		assertThat(result.stepMatches().get(1).getStartIndex()).isEqualTo(2);
	}

	@Test
	@DisplayName("マッチがない場合は空のリストを返す")
	void testNoMatch() {
		String content = """
				find: BOOL
				replace: bool
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		List<AstNode> tokenNodes = nodes("int", "x", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("空のルールリストでは空の結果を返す")
	void testEmptyRules() {
		List<AstNode> tokenNodes = nodes("int", "x", ";");
		List<MultiReplaceMatchResult> results = matcher.matchAll(List.of(), tokenNodes);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("空のトークンリストでは空の結果を返す")
	void testEmptyTokenList() {
		String content = """
				find: BOOL
				replace: bool
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, List.of());
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("ABSTRACT_PARAM を含む find spec にマッチする")
	void testAbstractParamMatch() {
		String content = """
				find: AfxMessageBox ( ABSTRACT_PARAM00 )
				replace: MessageBox.Show(ABSTRACT_PARAM00)
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		List<AstNode> tokenNodes = nodes("AfxMessageBox", "(", "\"hello\"", ")");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);
		assertThat(results).hasSize(1);
		assertThat(results.get(0).captures()).containsKey(0);
		assertThat(results.get(0).captures().get(0)).containsExactly("\"hello\"");
	}

	@Test
	@DisplayName("2 つの skip: で 3 find spec が離れた位置にマッチする")
	void testDoubleSkipMatch() {
		String content = """
				find: A
				replace: a
				skip:
				find: B
				replace: b
				skip:
				find: C
				replace: c
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// A at 0, B at 2, C at 4 (tokens between are skipped)
		List<AstNode> tokenNodes = nodes("A", "x", "B", "y", "C", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).hasSize(1);
		MultiReplaceMatchResult result = results.get(0);
		assertThat(result.stepMatches()).hasSize(3);
		assertThat(result.stepMatches().get(0).getStartIndex()).isEqualTo(0);
		assertThat(result.stepMatches().get(1).getStartIndex()).isEqualTo(2);
		assertThat(result.stepMatches().get(2).getStartIndex()).isEqualTo(4);
	}

	@Test
	@DisplayName("2 つの skip: で最後の find spec が存在しない場合はマッチしない")
	void testDoubleSkipNoMatchWhenLastMissing() {
		String content = """
				find: A
				replace: a
				skip:
				find: B
				replace: b
				skip:
				find: C
				replace: c
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// A at 0, B at 2, but C is absent
		List<AstNode> tokenNodes = nodes("A", "x", "B", "y", "D", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("2 つの skip: で中間の find spec が存在しない場合はマッチしない")
	void testDoubleSkipNoMatchWhenMiddleMissing() {
		String content = """
				find: A
				replace: a
				skip:
				find: B
				replace: b
				skip:
				find: C
				replace: c
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// A at 0, B absent, C at 3
		List<AstNode> tokenNodes = nodes("A", "x", "D", "C", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("2 つの skip: を持つ 3 spec 間で同じ ABSTRACT_PARAM が一致するときだけマッチする")
	void testDoubleSkipSharedAbstractParam() {
		String content = """
				scope: block
				find: TYPE ABSTRACT_PARAM00 ;
				replace: string ABSTRACT_PARAM00 ;
				skip:
				find: ABSTRACT_PARAM00 = init ( ) ;
				replace: ABSTRACT_PARAM00 = "" ;
				skip:
				find: use ( ABSTRACT_PARAM00 ) ;
				replace: consume(ABSTRACT_PARAM00);
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		List<AstNode> tokenNodes = nodes("{", "TYPE", "pszX", ";", // spec 0
				"noop", ";", // skipped
				"pszX", "=", "init", "(", ")", ";", // spec 1
				"log", ";", // skipped
				"use", "(", "pszX", ")", ";", // spec 2
				"}");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).captures().get(0)).containsExactly("pszX");
		assertThat(results.get(0).stepMatches()).hasSize(3);
	}

	@Test
	@DisplayName("2 つの skip: を持つ 3 spec 間で ABSTRACT_PARAM の値が異なるとマッチしない")
	void testDoubleSkipSharedAbstractParamMismatch() {
		String content = """
				scope: block
				find: TYPE ABSTRACT_PARAM00 ;
				replace: string ABSTRACT_PARAM00 ;
				skip:
				find: ABSTRACT_PARAM00 = init ( ) ;
				replace: ABSTRACT_PARAM00 = "" ;
				skip:
				find: use ( ABSTRACT_PARAM00 ) ;
				replace: consume(ABSTRACT_PARAM00);
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// spec 0 captures pszX, spec 2 uses pszY → mismatch
		List<AstNode> tokenNodes = nodes("{", "TYPE", "pszX", ";", "pszX", "=", "init", "(", ")", ";", "use", "(",
				"pszY", ")", ";", "}");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);
		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("連続 + skip: の混在パターン（spec0-spec1 連続、spec1-spec2 skip）でマッチする")
	void testMixedConsecutiveAndSkipMatch() {
		String content = """
				find: A
				replace: a
				find: B
				replace: b
				skip:
				find: C
				replace: c
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// A at 0, B at 1 (consecutive), C at 3 (with skip)
		List<AstNode> tokenNodes = nodes("A", "B", "x", "C", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).hasSize(1);
		assertThat(result(results, 0).stepMatches().get(0).getStartIndex()).isEqualTo(0);
		assertThat(result(results, 0).stepMatches().get(1).getStartIndex()).isEqualTo(1);
		assertThat(result(results, 0).stepMatches().get(2).getStartIndex()).isEqualTo(3);
	}

	@Test
	@DisplayName("連続 + skip: の混在で spec1 が連続していない場合はマッチしない")
	void testMixedConsecutiveAndSkipNoMatch() {
		String content = """
				find: A
				replace: a
				find: B
				replace: b
				skip:
				find: C
				replace: c
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		// A at 0, B at 2 (not consecutive with A), C at 4
		List<AstNode> tokenNodes = nodes("A", "x", "B", "x", "C", ";");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);
		assertThat(results).isEmpty();
	}

	/** ヘルパー: results.get(index) */
	private MultiReplaceMatchResult result(List<MultiReplaceMatchResult> results, int index) {
		return results.get(index);
	}

	@Test
	@DisplayName("複数 spec 間で同じ ABSTRACT_PARAM の値が一致するときだけマッチする")
	void testSharedAbstractParamAcrossSpecs() {
		String content = """
				scope: block
				find: LPSTR ABSTRACT_PARAM00 ;
				replace: string ABSTRACT_PARAM00 = "" ;
				skip:
				find: ABSTRACT_PARAM00 = ( char * ) malloc ( ABSTRACT_PARAM01 ) ;
				replace:
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		List<AstNode> tokenNodes = nodes("{", "LPSTR", "pszName", ";", "pszName", "=", "(", "char", "*", ")", "malloc",
				"(", "256", ")", ";", "}");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).hasSize(1);
		assertThat(results.get(0).captures().get(0)).containsExactly("pszName");
		assertThat(results.get(0).stepMatches()).hasSize(2);
	}

	@Test
	@DisplayName("複数 spec 間で同じ ABSTRACT_PARAM の値が異なるとマッチしない")
	void testSharedAbstractParamMismatchAcrossSpecs() {
		String content = """
				scope: block
				find: LPSTR ABSTRACT_PARAM00 ;
				replace: string ABSTRACT_PARAM00 = "" ;
				skip:
				find: ABSTRACT_PARAM00 = ( char * ) malloc ( ABSTRACT_PARAM01 ) ;
				replace:
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		List<AstNode> tokenNodes = nodes("{", "LPSTR", "pszName", ";", "pszOther", "=", "(", "char", "*", ")", "malloc",
				"(", "256", ")", ";", "}");

		List<MultiReplaceMatchResult> results = matcher.matchAll(rules, tokenNodes);

		assertThat(results).isEmpty();
	}
}
