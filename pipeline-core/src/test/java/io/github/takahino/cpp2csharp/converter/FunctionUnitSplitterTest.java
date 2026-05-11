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

package io.github.takahino.cpp2csharp.converter;

import io.github.takahino.cpp2csharp.tree.AstNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.github.takahino.cpp2csharp.converter.UnitLabel.BODY;
import static io.github.takahino.cpp2csharp.converter.UnitLabel.GAP;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@link FunctionUnitSplitter} のユニットテスト。
 */
@DisplayName("FunctionUnitSplitter")
class FunctionUnitSplitterTest {

	/** スペース区切りトークン列を AstNode リストに変換するヘルパー（streamIndex = list index） */
	private static List<AstNode> nodes(String tokens) {
		String[] parts = tokens.trim().split("\\s+");
		List<AstNode> result = new ArrayList<>();
		for (int i = 0; i < parts.length; i++) {
			result.add(AstNode.tokenNode(parts[i], 1, i, i));
		}
		return result;
	}

	private static List<String> texts(List<AstNode> nodes) {
		return nodes.stream().map(AstNode::getText).toList();
	}

	// ---- フォールバック（functionRanges = 空リスト） ----

	@Test
	@DisplayName("空トークンリスト: 空を返す")
	void emptyInput() {
		assertThat(FunctionUnitSplitter.split(List.of(), List.of())).isEmpty();
	}

	@Test
	@DisplayName("functionRanges が空: 全トークンを 1 つの body 単位として返す（フォールバック）")
	void fallbackToSingleUnit() {
		List<AstNode> tokens = nodes("void f ( ) { return ; } int x ;");
		List<TokenUnit> units = FunctionUnitSplitter.split(tokens, List.of());

		assertThat(units).hasSize(1);
		assertThat(units.get(0).label()).isEqualTo(BODY);
		assertThat(texts(units.get(0).tokens())).containsExactly("void", "f", "(", ")", "{", "return", ";", "}", "int",
				"x", ";");
	}

	// ---- ParseTree ベース分割 ----

	@Test
	@DisplayName("1 関数: シグネチャとボディが 1 つの body 単位になる")
	void singleFunction() {
		// tokens: void(0) f(1) ((2) )(3) {(4) return(5) ;(6) }(7)
		// functionRange: [0, 7]
		List<AstNode> tokens = nodes("void f ( ) { return ; }");
		List<int[]> ranges = List.of(new int[]{0, 7});
		List<TokenUnit> units = FunctionUnitSplitter.split(tokens, ranges);

		assertThat(units).hasSize(1);
		assertThat(units.get(0).label()).isEqualTo(BODY);
		assertThat(texts(units.get(0).tokens())).containsExactly("void", "f", "(", ")", "{", "return", ";", "}");
	}

	@Test
	@DisplayName("2 関数: body + body の 2 単位")
	void twoFunctions() {
		// int(0) f(1) ((2) )(3) {(4) return(5) 1(6) ;(7) }(8) int(9) g(10) ((11) )(12)
		// {(13) return(14) 2(15) ;(16) }(17)
		List<AstNode> tokens = nodes("int f ( ) { return 1 ; } int g ( ) { return 2 ; }");
		List<int[]> ranges = List.of(new int[]{0, 8}, new int[]{9, 17});
		List<TokenUnit> units = FunctionUnitSplitter.split(tokens, ranges);

		assertThat(units).hasSize(2);
		assertThat(units.get(0).label()).isEqualTo(BODY);
		assertThat(texts(units.get(0).tokens())).containsExactly("int", "f", "(", ")", "{", "return", "1", ";", "}");
		assertThat(units.get(1).label()).isEqualTo(BODY);
		assertThat(texts(units.get(1).tokens())).containsExactly("int", "g", "(", ")", "{", "return", "2", ";", "}");
	}

	@Test
	@DisplayName("関数の前後にグローバル宣言: gap + body + gap の 3 単位")
	void gapBodyGap() {
		// int(0) g(1) ;(2) void(3) f(4) ((5) )(6) {(7) }(8) int(9) x(10) ;(11)
		List<AstNode> tokens = nodes("int g ; void f ( ) { } int x ;");
		List<int[]> ranges = List.of(new int[]{3, 8});
		List<TokenUnit> units = FunctionUnitSplitter.split(tokens, ranges);

		assertThat(units).hasSize(3);
		assertThat(units.get(0).label()).isEqualTo(GAP);
		assertThat(texts(units.get(0).tokens())).containsExactly("int", "g", ";");
		assertThat(units.get(1).label()).isEqualTo(BODY);
		assertThat(texts(units.get(1).tokens())).containsExactly("void", "f", "(", ")", "{", "}");
		assertThat(units.get(2).label()).isEqualTo(GAP);
		assertThat(texts(units.get(2).tokens())).containsExactly("int", "x", ";");
	}

	@Test
	@DisplayName("全単位を結合すると元のトークン列と一致する")
	void combinedEqualsOriginal() {
		List<AstNode> tokens = nodes("int g ; void f ( ) { sin ( x ) ; } int h ;");
		List<int[]> ranges = List.of(new int[]{3, 13});
		List<TokenUnit> units = FunctionUnitSplitter.split(tokens, ranges);

		List<String> combined = units.stream().flatMap(u -> u.tokens().stream()).map(AstNode::getText).toList();
		List<String> original = tokens.stream().map(AstNode::getText).toList();

		assertThat(combined).isEqualTo(original);
	}
}
