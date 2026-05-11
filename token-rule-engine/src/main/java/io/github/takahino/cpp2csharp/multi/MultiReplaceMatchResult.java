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

import io.github.takahino.cpp2csharp.matcher.MatchResult;
import io.github.takahino.cpp2csharp.mrule.MultiReplaceRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * マルチ置換マッチの結果を保持するレコード。
 *
 * <p>
 * 1つの {@link MultiReplaceRule} に対する全 find spec のマッチ結果と、 共有キャプチャを保持する。
 * </p>
 *
 * @param rule
 *            マッチしたルール
 * @param stepMatches
 *            各 find spec のマッチ結果リスト（順序は spec 順）
 * @param captures
 *            全 step の共有キャプチャ（パラメータインデックス → トークンリスト）
 * @param blockStart
 *            ブロックスコープ時のブロック開始インデックス（-1 = 非ブロック）
 * @param blockEnd
 *            ブロックスコープ時のブロック終了インデックス（-1 = 非ブロック）
 */
public record MultiReplaceMatchResult(MultiReplaceRule rule, List<MatchResult> stepMatches,
		Map<Integer, List<String>> captures, int blockStart, int blockEnd) {

	/**
	 * 最小の開始インデックスを返す（挿入位置として使用）。
	 *
	 * @return 最小開始インデックス
	 */
	public int insertionIndex() {
		return stepMatches.stream().mapToInt(MatchResult::getStartIndex).min().orElse(0);
	}

	/**
	 * 全マッチ範囲を降順（安全な削除順）で返す。
	 *
	 * @return {@code [startIndex, endIndex]} の配列リスト（降順）
	 */
	public List<int[]> allMatchedRanges() {
		List<int[]> ranges = new ArrayList<>();
		for (MatchResult m : stepMatches) {
			ranges.add(new int[]{m.getStartIndex(), m.getEndIndex()});
		}
		ranges.sort((a, b) -> Integer.compare(b[0], a[0]));
		return ranges;
	}

	/**
	 * 最初の step のマッチに対する展開済み置換テキストを返す。
	 *
	 * @return 展開済み置換テキスト
	 */
	public String expandedReplacement() {
		if (stepMatches.isEmpty())
			return "";
		return stepMatches.get(0).getExpandedToTemplate();
	}
}
