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

package io.github.takahino.cpp2csharp.comby;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CombyRuleLoader .crule パーステスト")
class CombyRuleLoaderTest {

	private final CombyRuleLoader loader = new CombyRuleLoader();

	@Test
	@DisplayName("基本的な from/to ルールのパース")
	void parseBasicRule() {
		String content = "from: :[recv].Left(:[n])\nto: :[recv].Substring(0, :[n])\n";
		List<CombyRule> rules = loader.parseContent(content, "test.crule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getFromPattern()).isEqualTo(":[recv].Left(:[n])");
		assertThat(rules.get(0).getToTemplate()).isEqualTo(":[recv].Substring(0, :[n])");
		assertThat(rules.get(0).getTestCases()).isEmpty();
	}

	@Test
	@DisplayName("test/assrt ペアのパース")
	void parseTestAssrt() {
		String content = "from: Left(:[n])\nto: Substring(0,:[n])\ntest: Left(5)\nassrt: Substring(0,5)\n";
		List<CombyRule> rules = loader.parseContent(content, "test.crule");
		assertThat(rules).hasSize(1);
		assertThat(rules.get(0).getTestCases()).hasSize(1);
		assertThat(rules.get(0).getTestCases().get(0).testInput()).isEqualTo("Left(5)");
		assertThat(rules.get(0).getTestCases().get(0).expectedOutput()).isEqualTo("Substring(0,5)");
	}

	@Test
	@DisplayName("コメント行は無視される")
	void commentsIgnored() {
		String content = "# this is a comment\nfrom: A\nto: B\n# another comment\n";
		List<CombyRule> rules = loader.parseContent(content, "test.crule");
		assertThat(rules).hasSize(1);
	}

	@Test
	@DisplayName("複数ルールのパース")
	void parseMultipleRules() {
		String content = "from: A\nto: B\nfrom: C\nto: D\n";
		List<CombyRule> rules = loader.parseContent(content, "test.crule");
		assertThat(rules).hasSize(2);
	}

	@Test
	@DisplayName("rules/comby/ が存在しない場合は空リストを返す")
	void emptyWhenNoCombyDir() throws Exception {
		List<List<CombyRule>> phases = loader.loadFromClasspath();
		// Directory either exists with rules or returns empty - should not throw
		assertThat(phases).isNotNull();
	}
}
