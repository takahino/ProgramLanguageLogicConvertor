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

package io.github.takahino.cpp2csharp.dynamic;

import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionToken;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DynamicRuleLoader .drule パーステスト")
class DynamicRuleLoaderTest {

	private final DynamicRuleLoader loader = new DynamicRuleLoader(CppParserFactory.asLexerFactory());

	@Test
	@DisplayName("collect/from/to の基本パース")
	void parseBasicSpec() {
		String content = """
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )
				to: private void ABSTRACT_PARAM00(ABSTRACT_PARAM01)
				""";
		DynamicRuleSpec spec = loader.parse(content, "test.drule");

		assertThat(spec).isNotNull();
		assertThat(spec.sourceFile()).isEqualTo("test.drule");
		assertThat(spec.collectPattern()).hasSize(2); // ABSTRACT_PARAM00, ::
		assertThat(spec.templates()).hasSize(1);
		assertThat(spec.templates().get(0).fromTemplate())
				.isEqualTo("void COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )");
		assertThat(spec.templates().get(0).toTemplate()).isEqualTo("private void ABSTRACT_PARAM00(ABSTRACT_PARAM01)");
	}

	@Test
	@DisplayName("collect パターンが ABSTRACT_PARAM00 をトークンとして含む")
	void collectPatternContainsAbstractParam() {
		String content = """
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""";
		DynamicRuleSpec spec = loader.parse(content, "test.drule");

		assertThat(spec.collectPattern()).isNotEmpty();
		assertThat(spec.collectPattern().get(0).isAbstractParam()).isTrue();
		assertThat(spec.collectPattern().get(0).getParamIndex()).isEqualTo(0);
	}

	@Test
	@DisplayName("コメント行は無視される")
	void commentsIgnored() {
		String content = """
				# クラス名を収集する
				collect: ABSTRACT_PARAM00 ::
				# void メソッド
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""";
		DynamicRuleSpec spec = loader.parse(content, "test.drule");

		assertThat(spec).isNotNull();
		assertThat(spec.templates()).hasSize(1);
	}

	@Test
	@DisplayName("複数の from/to テンプレートをパースできる")
	void parseMultipleTemplates() {
		String content = """
				collect: ABSTRACT_PARAM00 ::
				from: void COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )
				to: private void ABSTRACT_PARAM00(ABSTRACT_PARAM01)
				from: bool COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )
				to: private bool ABSTRACT_PARAM00(ABSTRACT_PARAM01)
				from: int COLLECTED :: ABSTRACT_PARAM00 ( ABSTRACT_PARAM01 )
				to: private int ABSTRACT_PARAM00(ABSTRACT_PARAM01)
				""";
		DynamicRuleSpec spec = loader.parse(content, "test.drule");

		assertThat(spec.templates()).hasSize(3);
		assertThat(spec.templates().get(1).fromTemplate()).startsWith("bool COLLECTED");
		assertThat(spec.templates().get(2).fromTemplate()).startsWith("int COLLECTED");
	}

	@Test
	@DisplayName("collect がない場合は null を返す")
	void missingCollectReturnsNull() {
		String content = """
				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()
				""";
		DynamicRuleSpec spec = loader.parse(content, "test.drule");

		assertThat(spec).isNull();
	}

	@Test
	@DisplayName("from/to テンプレートがない場合は null を返す")
	void missingTemplatesReturnsNull() {
		String content = "collect: ABSTRACT_PARAM00 ::\n";
		DynamicRuleSpec spec = loader.parse(content, "test.drule");

		assertThat(spec).isNull();
	}

	@Test
	@DisplayName("空行は無視される")
	void emptyLinesIgnored() {
		String content = """

				collect: ABSTRACT_PARAM00 ::

				from: void COLLECTED :: ABSTRACT_PARAM00 ( )
				to: private void ABSTRACT_PARAM00()

				""";
		DynamicRuleSpec spec = loader.parse(content, "test.drule");

		assertThat(spec).isNotNull();
		assertThat(spec.templates()).hasSize(1);
	}
}
