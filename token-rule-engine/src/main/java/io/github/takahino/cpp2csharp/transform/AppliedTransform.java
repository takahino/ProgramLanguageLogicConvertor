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

package io.github.takahino.cpp2csharp.transform;

import java.util.List;

/**
 * 1 件の変換適用を記録するレコード。
 *
 * <p>
 * レポートで「どのルールが」「どのノードに」「どのように変換されたか」を 時系列で表示するために使用する。
 * </p>
 *
 * @param sequence
 *            適用順序（1から始まる通し番号）
 * @param phaseIndex
 *            フェーズ番号（0始まり）
 * @param ruleSource
 *            ルール定義元ファイル名
 * @param ruleFrom
 *            from パターン（文字列表現）
 * @param ruleTo
 *            to テンプレート（文字列表現）
 * @param matchedNode
 *            マッチしたノードの文字列表現（変換前）
 * @param transformedTo
 *            変換後の文字列
 * @param sourceLineNumber
 *            元ソースの行番号（1始まり、不明時は 0）
 * @param lineBefore
 *            該当行の置き換え前の文字列（空の場合は未設定）
 * @param lineAfter
 *            該当行の置き換え後の文字列（空の場合は未設定）
 * @param selectedStrategy
 *            選択に使用した戦略名
 * @param fallbackFrom
 *            フォールバック元の戦略名（フォールバック時のみ、それ以外は null）
 * @param selectionReason
 *            選択理由の要約
 * @param selectionDetails
 *            選択の詳細（スコア導出・同点時情報・対決相手など、戦略が出力する場合）
 * @param startIndex
 *            マッチ開始位置（フラットトークンリスト内のインデックス、不明時は -1）
 * @param endIndex
 *            マッチ終了位置（exclusive、不明時は -1）
 * @param mergedIds
 *            置換でマージされた id のリスト（Excel 可視化のセルコメント用）
 */
public record AppliedTransform(int sequence, int phaseIndex, String ruleSource, String ruleFrom, String ruleTo,
		String matchedNode, String transformedTo, int sourceLineNumber, String lineBefore, String lineAfter,
		String selectedStrategy, String fallbackFrom, String selectionReason, String selectionDetails, int startIndex,
		int endIndex, List<Integer> mergedIds) {

	/**
	 * 後方互換のためのコンストラクタ（sourceLineNumber 以降なし）。
	 *
	 * @deprecated 新規コードでは lineBefore, lineAfter, selectedStrategy 等を指定すること
	 */
	@Deprecated
	public AppliedTransform(int sequence, int phaseIndex, String ruleSource, String ruleFrom, String matchedNode,
			String transformedTo, int sourceLineNumber) {
		this(sequence, phaseIndex, ruleSource, ruleFrom, "", matchedNode, transformedTo, sourceLineNumber, "", "", "",
				null, "", null, -1, -1, List.of());
	}
}
