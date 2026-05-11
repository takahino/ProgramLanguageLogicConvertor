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

import java.util.ArrayList;
import java.util.List;

/**
 * トークン列を関数定義単位に分割するクラス。
 *
 * <h2>分割方式</h2>
 * <p>
 * ParseTree から取得した関数定義の streamIndex 範囲を用いて、 各関数定義（シグネチャ＋ボディ）を "body" 単位として切り出す。
 * 関数定義外のトークンは "gap" 単位にまとめる。
 * </p>
 *
 * <h2>フォールバック（ParseTree 範囲なし）</h2>
 * <p>
 * {@code functionRanges} が空の場合（パースエラー等で ParseTree 取得不可）は、 トークン列全体を1つの "body"
 * 単位として返す。
 * </p>
 *
 * <h2>PRE フェーズ合成トークンの扱い</h2>
 * <p>
 * PRE フェーズで生成された合成トークン（streamIndex=-1）が gap モード中に現れた場合、 次の body 開始まで保留して body
 * に取り込む。 これにより、{@code CString→string} のように PRE がリターン型を合成トークンに置換した場合でも
 * シグネチャトークンが正しく body unit に含まれる。
 * </p>
 */
public final class FunctionUnitSplitter {

	private FunctionUnitSplitter() {
	}

	/**
	 * ParseTree から得た関数定義範囲を用いてトークン列を {@link TokenUnit} に分割する。
	 *
	 * <p>
	 * 各 {@code int[2]} = [startStreamIndex, stopStreamIndex] が 1 つの関数定義に対応する。
	 * {@code functionRanges} が空の場合はトークン列全体を1つの "body" 単位として返す。
	 * </p>
	 *
	 * @param tokens
	 *            ファイル全体のトークンノード列（PRE フェーズ後のもの）
	 * @param functionRanges
	 *            各関数定義の [startStreamIndex, stopStreamIndex] のリスト
	 * @return 処理単位のリスト（元の順序を保つ）
	 */
	public static List<TokenUnit> split(List<AstNode> tokens, List<int[]> functionRanges) {
		if (tokens.isEmpty()) {
			return List.of();
		}
		if (functionRanges.isEmpty()) {
			// ParseTree が取得できなかった場合: 全体を1ユニットとして処理
			return List.of(new TokenUnit(UnitLabel.BODY, List.copyOf(tokens)));
		}

		// functionRanges は ParseTree DFS 走査（ソース順）で構築されているため、既にソート済み
		List<TokenUnit> units = new ArrayList<>();
		List<AstNode> gap = new ArrayList<>();
		// PRE フェーズの合成トークン（streamIdx=-1）用バッファ:
		// gap モード中に合成トークンが現れた場合、次の body 開始まで保留し body に取り込む。
		List<AstNode> syntheticBuffer = new ArrayList<>();
		List<AstNode> body = null;
		int currentEnd = -1;
		int rangeIdx = 0;

		for (AstNode token : tokens) {
			int streamIdx = token.getStreamIndex();

			if (body != null) {
				// 現在 body 単位に属している
				body.add(token);
				if (streamIdx >= 0 && streamIdx >= currentEnd) {
					// body 単位終了
					units.add(new TokenUnit(UnitLabel.BODY, List.copyOf(body)));
					body = null;
					currentEnd = -1;
					rangeIdx++;
				}
			} else {
				// gap 中: 次の関数定義開始を探す
				if (streamIdx < 0) {
					// 合成トークン: 次の body が始まるまでバッファ
					syntheticBuffer.add(token);
				} else if (rangeIdx < functionRanges.size() && streamIdx >= functionRanges.get(rangeIdx)[0]) {
					// body 開始: gap を flush し、合成トークンバッファを body の先頭に取り込む
					if (!gap.isEmpty()) {
						gap.addAll(syntheticBuffer);
						syntheticBuffer.clear();
						units.add(new TokenUnit(UnitLabel.GAP, List.copyOf(gap)));
						gap.clear();
					}
					body = new ArrayList<>(syntheticBuffer);
					syntheticBuffer.clear();
					body.add(token);
					currentEnd = functionRanges.get(rangeIdx)[1];
					// streamIdx が既に currentEnd 以上なら 1 トークンで body 完了
					if (streamIdx >= currentEnd) {
						units.add(new TokenUnit(UnitLabel.BODY, List.copyOf(body)));
						body = null;
						currentEnd = -1;
						rangeIdx++;
					}
				} else {
					// 通常 gap トークン: 合成バッファを gap に移してから追加
					gap.addAll(syntheticBuffer);
					syntheticBuffer.clear();
					gap.add(token);
				}
			}
		}

		// 終端処理
		if (body != null) {
			// 閉じトークンが token list に含まれなかった場合（パースエラー等）
			gap.addAll(body);
		}
		// 未処理の合成バッファは gap へ
		gap.addAll(syntheticBuffer);
		if (!gap.isEmpty()) {
			units.add(new TokenUnit(UnitLabel.GAP, List.copyOf(gap)));
		}
		return units;
	}
}
