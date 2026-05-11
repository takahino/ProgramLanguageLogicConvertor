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

package io.github.takahino.cpp2csharp.mrule;

import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link MultiReplaceRuleLoader} のユニットテスト。
 */
@DisplayName("MultiReplaceRuleLoader テスト")
class MultiReplaceRuleLoaderTest {

	private final MultiReplaceRuleLoader loader = new MultiReplaceRuleLoader(CppParserFactory.asLexerFactory());

	@Test
	@DisplayName("単純な find/replace ペアをパースできる")
	void testSimpleFindReplace() {
		String content = """
				find: BOOL
				replace: bool
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(1);
		MultiReplaceRule rule = rules.get(0);
		assertThat(rule.getScope()).isEqualTo(MRuleScope.NONE);
		assertThat(rule.getFindSpecs()).hasSize(1);
		MRuleFindSpec spec = rule.getFindSpecs().get(0);
		assertThat(spec.pattern()).hasSize(1);
		assertThat(spec.pattern().get(0).getValue()).isEqualTo("BOOL");
		assertThat(spec.replacement()).isEqualTo("bool");
		assertThat(spec.skipBefore()).isFalse();
	}

	@Test
	@DisplayName("複数の find/replace ペアをパースできる（空行区切り）")
	void testMultipleRules() {
		String content = """
				find: BOOL
				replace: bool

				find: NULL
				replace: null
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(2);
		assertThat(rules.get(0).getFindSpecs().get(0).replacement()).isEqualTo("bool");
		assertThat(rules.get(1).getFindSpecs().get(0).replacement()).isEqualTo("null");
	}

	@Test
	@DisplayName("scope: block が正しくパースされる")
	void testScopeBlock() {
		String content = """
				scope: block
				find: BOOL
				replace: bool
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getScope()).isEqualTo(MRuleScope.BLOCK);
	}

	@Test
	@DisplayName("skip: が次の find の skipBefore=true を設定する")
	void testSkipBeforeFlag() {
		String content = """
				find: BOOL ( ABSTRACT_PARAM00 )
				replace: bool(ABSTRACT_PARAM00)
				skip:
				find: return ABSTRACT_PARAM00
				replace: return ABSTRACT_PARAM00
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(1);
		MultiReplaceRule rule = rules.get(0);
		assertThat(rule.getFindSpecs()).hasSize(2);
		assertThat(rule.getFindSpecs().get(0).skipBefore()).isFalse();
		assertThat(rule.getFindSpecs().get(1).skipBefore()).isTrue();
	}

	@Test
	@DisplayName("コメント行はスキップされる")
	void testCommentLines() {
		String content = """
				# This is a comment
				find: BOOL
				# Another comment
				replace: bool
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getFindSpecs().get(0).replacement()).isEqualTo("bool");
	}

	@Test
	@DisplayName("空のコンテンツでルールが空リストになる")
	void testEmptyContent() {
		List<MultiReplaceRule> rules = loader.loadFromString("", "test.mrule");
		assertThat(rules).isEmpty();
	}

	@Test
	@DisplayName("コメントのみのコンテンツでルールが空リストになる")
	void testOnlyComments() {
		String content = """
				# comment 1
				# comment 2
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).isEmpty();
	}

	@Test
	@DisplayName("複数 find spec を持つルールをパースできる（連続マッチ）")
	void testMultipleSpecsSingleRule() {
		String content = """
				find: BOOL ( ABSTRACT_PARAM00 )
				replace: bool(ABSTRACT_PARAM00)
				find: return ABSTRACT_PARAM00
				replace: return ABSTRACT_PARAM00
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getFindSpecs()).hasSize(2);
		assertThat(rules.get(0).getFindSpecs().get(0).skipBefore()).isFalse();
		assertThat(rules.get(0).getFindSpecs().get(1).skipBefore()).isFalse();
	}

	@Test
	@DisplayName("ルール ID が sourceFile:ruleIndex 形式で生成される")
	void testRuleIdFormat() {
		String content = """
				find: BOOL
				replace: bool

				find: NULL
				replace: null
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules.get(0).getRuleId()).isEqualTo("test.mrule:0");
		assertThat(rules.get(1).getRuleId()).isEqualTo("test.mrule:1");
	}

	@Test
	@DisplayName("scope: none が正しくパースされる")
	void testScopeNone() {
		String content = """
				scope: none
				find: BOOL
				replace: bool
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getScope()).isEqualTo(MRuleScope.NONE);
	}

	@Test
	@DisplayName("複数 skip: が並ぶとき各 find spec の skipBefore が正しく設定される")
	void testMultipleSkipFlags() {
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
		assertThat(rules).hasSize(1);
		MultiReplaceRule rule = rules.get(0);
		assertThat(rule.getFindSpecs()).hasSize(3);
		assertThat(rule.getFindSpecs().get(0).skipBefore()).isFalse();
		assertThat(rule.getFindSpecs().get(1).skipBefore()).isTrue();
		assertThat(rule.getFindSpecs().get(2).skipBefore()).isTrue();
	}

	@Test
	@DisplayName("連続後 skip: のパターンで skipBefore フラグが正しく設定される")
	void testMixedConsecutiveAndSkip() {
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
		assertThat(rules).hasSize(1);
		MultiReplaceRule rule = rules.get(0);
		assertThat(rule.getFindSpecs()).hasSize(3);
		assertThat(rule.getFindSpecs().get(0).skipBefore()).isFalse();
		assertThat(rule.getFindSpecs().get(1).skipBefore()).isFalse();
		assertThat(rule.getFindSpecs().get(2).skipBefore()).isTrue();
	}

	@Test
	@DisplayName("ABSTRACT_PARAM を含む find パターンが正しくトークン化される")
	void testAbstractParamInPattern() {
		String content = """
				find: AfxMessageBox ( ABSTRACT_PARAM00 )
				replace: MessageBox.Show(ABSTRACT_PARAM00)
				""";
		List<MultiReplaceRule> rules = loader.loadFromString(content, "test.mrule");
		assertThat(rules).hasSize(1);
		MRuleFindSpec spec = rules.get(0).getFindSpecs().get(0);
		assertThat(spec.pattern()).hasSize(4); // AfxMessageBox ( ABSTRACT_PARAM00 )
		assertThat(spec.pattern().get(2).isAbstractParam()).isTrue();
		assertThat(spec.pattern().get(2).getParamIndex()).isEqualTo(0);
	}
}
