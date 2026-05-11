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

package io.github.takahino.cpp2csharp.rule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link ConversionToken} のユニットテスト。
 */
@DisplayName("ConversionToken テスト")
class ConversionTokenTest {

	@Test
	@DisplayName("具体的なトークンを正しく生成できる")
	void testConcreteToken() {
		ConversionToken token = ConversionToken.of("AfxMessageBox");
		assertThat(token.getValue()).isEqualTo("AfxMessageBox");
		assertThat(token.isAbstractParam()).isFalse();
		assertThat(token.getParamIndex()).isEqualTo(-1);
	}

	@Test
	@DisplayName("ABSTRACT_PARAM00 を抽象化トークンとして生成できる")
	void testAbstractParam00() {
		ConversionToken token = ConversionToken.of("ABSTRACT_PARAM00");
		assertThat(token.getValue()).isEqualTo("ABSTRACT_PARAM00");
		assertThat(token.isAbstractParam()).isTrue();
		assertThat(token.getParamIndex()).isEqualTo(0);
	}

	@Test
	@DisplayName("ABSTRACT_PARAM99 を抽象化トークンとして生成できる")
	void testAbstractParam99() {
		ConversionToken token = ConversionToken.of("ABSTRACT_PARAM99");
		assertThat(token.isAbstractParam()).isTrue();
		assertThat(token.getParamIndex()).isEqualTo(99);
	}

	@Test
	@DisplayName("ABSTRACT_PARAM01 を正しくパースできる")
	void testAbstractParam01() {
		ConversionToken token = ConversionToken.of("ABSTRACT_PARAM01");
		assertThat(token.isAbstractParam()).isTrue();
		assertThat(token.getParamIndex()).isEqualTo(1);
	}

	@Test
	@DisplayName("特殊文字トークンを正しく生成できる")
	void testSpecialCharTokens() {
		assertThat(ConversionToken.of(".").getValue()).isEqualTo(".");
		assertThat(ConversionToken.of("(").getValue()).isEqualTo("(");
		assertThat(ConversionToken.of(";").getValue()).isEqualTo(";");
		assertThat(ConversionToken.of("|").getValue()).isEqualTo("|");
	}

	@Test
	@DisplayName("null 値の場合は例外がスローされる")
	void testNullThrowsException() {
		assertThatThrownBy(() -> ConversionToken.of(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("空文字列の場合は例外がスローされる")
	void testEmptyThrowsException() {
		assertThatThrownBy(() -> ConversionToken.of("")).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("equals が同値のトークンで true を返す")
	void testEquals() {
		assertThat(ConversionToken.of("this")).isEqualTo(ConversionToken.of("this"));
		assertThat(ConversionToken.of("ABSTRACT_PARAM00")).isEqualTo(ConversionToken.of("ABSTRACT_PARAM00"));
	}

	@Test
	@DisplayName("toString が適切な文字列を返す")
	void testToString() {
		assertThat(ConversionToken.of("ABSTRACT_PARAM05").toString()).contains("ABSTRACT_PARAM[05]");
		assertThat(ConversionToken.of("AfxMessageBox").toString()).contains("AfxMessageBox");
	}
}
