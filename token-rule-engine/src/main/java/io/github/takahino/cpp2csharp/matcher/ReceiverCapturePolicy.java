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

package io.github.takahino.cpp2csharp.matcher;

import java.util.List;
import java.util.Set;

/**
 * RECEIVER[nn] の軽量プリフィルタ。
 *
 * <p>
 * AST に到達する前でも確実に不正と言える候補だけを落とす。 receiver として厳密に正しいかどうかの本判定は
 * {@link ReceiverAstValidator} が担う。 false negative を避けるため、迷うケースは AST に回す。
 * </p>
 *
 * <h2>プリフィルタで reject する条件</h2>
 * <ul>
 * <li>空</li>
 * <li>先頭が {@code .} / {@code ->} / {@code )} / {@code ]} / {@code }}</li>
 * <li>括弧不均衡</li>
 * <li>深さ 0 の {@code ;} または {@code ,}</li>
 * </ul>
 *
 * <p>
 * 単一トークン（合成置換トークン含む）は AST 再パース不可のため即 accept とする。
 * </p>
 *
 * @see ReceiverAstValidator
 *      <p>
 *      関連仕様: {@code docs/receiver_validation_spec.md}
 *      </p>
 */
public final class ReceiverCapturePolicy {

	private ReceiverCapturePolicy() {
		throw new AssertionError("utility class");
	}

	/**
	 * 先頭に出現を禁止するトークン。
	 *
	 * <p>
	 * {@code .} と {@code ->} はドットチェーン中間のメソッド名誤キャプチャを防ぐ。 {@code )} {@code ]}
	 * {@code } は閉じ括弧単体でレシーバーにならない。 {@code (} は通常モードでは誤キャプチャが多いため reject
	 * し、診断モードのみ許可する。
	 * </p>
	 */
	static final Set<String> INVALID_START_TOKENS = Set.of("(", ")", "]", "}", ".", "->");

	/**
	 * 深さ 0 で拒否するトークン（文境界）。
	 *
	 * <p>
	 * プリフィルタは最小責務のため、{@code +} {@code =} {@code ?} {@code :} 等は ここでは拒否せず AST に回す。
	 * </p>
	 */
	static final Set<String> REJECT_DEPTH0_TOKENS = Set.of(";", ",");

	/**
	 * プリフィルタ: AST に回す前に明白な不正だけを reject する。
	 *
	 * <p>
	 * 本メソッドが true を返しても receiver として有効とは限らない。 呼び出し側で
	 * {@link ReceiverAstValidator#isValid} による本判定を行う。
	 * </p>
	 *
	 * @param captured
	 *            RECEIVER にキャプチャされたトークン列
	 * @return 明白に不正でなければ true（AST に回す、または単一トークンで即 accept）
	 */
	public static boolean passesPrefilter(List<String> captured) {
		return validate(captured, false);
	}

	/**
	 * 診断モード用プリフィルタ: 括弧始まりも AST に回す。
	 *
	 * @param captured
	 *            RECEIVER にキャプチャされたトークン列
	 * @return 明白に不正でなければ true
	 */
	public static boolean passesPrefilterForDiagnostic(List<String> captured) {
		return validate(captured, true);
	}

	/**
	 * トークンが識別子（英字・数字・アンダースコアのみで構成）かどうかを返す。
	 *
	 * <p>
	 * 合成置換トークン（変換後の中間表現）は複数トークンを結合した文字列になりうるため、
	 * 英数字以外の文字を含む場合がある。このメソッドは純粋な識別子判定のみを行い、 合成置換トークンの許可は呼び出し側（単一トークン判定ロジック）が担う。
	 * </p>
	 *
	 * @param token
	 *            判定対象トークン文字列
	 * @return 識別子として有効であれば true
	 */
	public static boolean isIdentifierLike(String token) {
		if (token.isEmpty())
			return false;
		for (int i = 0; i < token.length(); i++) {
			char c = token.charAt(i);
			if (!Character.isLetterOrDigit(c) && c != '_')
				return false;
		}
		return true;
	}

	/**
	 * 内部検証ロジック。明白な不正だけを reject する。
	 *
	 * @param captured
	 *            検証対象トークン列
	 * @param allowBracketStart
	 *            true のとき先頭の括弧制約を外す（診断モード用）
	 * @return 明白に不正でなければ true
	 */
	private static boolean validate(List<String> captured, boolean allowBracketStart) {
		if (captured.isEmpty())
			return false;

		String first = captured.get(0);
		if (!allowBracketStart) {
			if (INVALID_START_TOKENS.contains(first))
				return false;
		}

		// 単一トークン: 合成置換トークンは AST 再パース不可のため即 pass
		if (captured.size() == 1)
			return true;

		// 複数トークン: 括弧均衡と深さ 0 の文境界のみチェック
		BracketDepthTracker tracker = new BracketDepthTracker();
		for (String t : captured) {
			if (!tracker.track(t))
				return false;
			if (tracker.atSurface() && REJECT_DEPTH0_TOKENS.contains(t))
				return false;
		}
		return tracker.isBalanced();
	}
}
