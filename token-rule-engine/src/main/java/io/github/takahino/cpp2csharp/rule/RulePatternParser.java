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

import java.util.List;

/**
 * 変換ルールの from パターンを解析し、引数個数などの構文情報を抽出するクラス。
 *
 * <p>
 * ブラケット深度追跡（{@link BracketDepthTracker}）を用いてトークン列から関数呼び出しの
 * 引数個数を求める。言語文法への依存なしに動作する。
 * </p>
 */
public final class RulePatternParser {

	private RulePatternParser() {
	}

	/**
	 * from パターンから期待する引数個数をトークン列解析で導出する。
	 *
	 * <p>
	 * 最初の {@code (} から対応する {@code )} までの範囲で、深さ 0 のカンマ数を数えて引数個数を返す。 引数がある場合は カンマ数 +
	 * 1、引数なしの空括弧は 0、括弧なしは -1 を返す。
	 * </p>
	 *
	 * @param fromTokens
	 *            from パターンのトークン列
	 * @return 期待する引数個数、括弧なしまたはパース失敗時は -1
	 */
	public static int parseArgumentCount(List<ConversionToken> fromTokens) {
		List<String> values = fromTokens.stream().map(ConversionToken::getValue).toList();
		if (values.stream().noneMatch("("::equals)) {
			return -1;
		}

		// Find the first '(' and count depth-0 commas until matching ')'
		int parenStart = -1;
		for (int i = 0; i < values.size(); i++) {
			if ("(".equals(values.get(i))) {
				parenStart = i;
				break;
			}
		}
		if (parenStart < 0) {
			return -1;
		}

		int depth = 0;
		int commaCount = 0;
		boolean hasContent = false;
		for (int i = parenStart; i < values.size(); i++) {
			String token = values.get(i);
			if ("(".equals(token) || "[".equals(token) || "{".equals(token)) {
				depth++;
			} else if (")".equals(token) || "]".equals(token) || "}".equals(token)) {
				depth--;
				if (depth == 0) {
					// reached closing paren
					if (!hasContent) {
						return 0;
					}
					return commaCount + 1;
				}
			} else if (",".equals(token) && depth == 1) {
				commaCount++;
				hasContent = true;
			} else if (depth == 1) {
				// any non-comma, non-bracket token inside the parens
				// ignore abstract params like ABSTRACT_PARAM00, RECEIVER
				String v = token;
				if (!v.startsWith("ABSTRACT_PARAM") && !v.startsWith("RECEIVER")) {
					hasContent = true;
				} else {
					hasContent = true; // abstract params also count
				}
			}
		}
		return -1;
	}
}
