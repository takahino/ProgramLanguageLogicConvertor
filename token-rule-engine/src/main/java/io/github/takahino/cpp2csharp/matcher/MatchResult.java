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

import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.rule.ConversionToken;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * パターンマッチングの結果を保持するクラス。
 *
 * <p>
 * マッチが成功した場合、各 ABSTRACT_PARAM[nn] が対応するトークン列 ({@code List<String>}) にキャプチャされる。
 * </p>
 */
public final class MatchResult {

	private static final Pattern ABSTRACT_PARAM_PATTERN = Pattern.compile("ABSTRACT_PARAM(\\d{2})");

	private static final Pattern ABSTRACT_TOKEN_PATTERN = Pattern.compile("ABSTRACT_TOKEN(\\d{2})");

	private static final Pattern RECEIVER_PATTERN = Pattern.compile("\\b" + ConversionToken.RECEIVER_TOKEN + "\\b");

	/** マッチしたルール */
	private final ConversionRule rule;

	/**
	 * ABSTRACT_PARAM のキャプチャ結果。 キー: パラメータインデックス (0〜99)、値: マッチしたトークン文字列リスト
	 */
	private final Map<Integer, List<String>> captures;

	/** マッチ開始位置 (フラットトークンリスト内のインデックス) */
	private final int startIndex;

	/** マッチ終了位置 (フラットトークンリスト内のインデックス、exclusive) */
	private final int endIndex;

	/**
	 * コンストラクタ。
	 *
	 * @param rule
	 *            マッチしたルール
	 * @param captures
	 *            ABSTRACT_PARAM のキャプチャ結果
	 * @param startIndex
	 *            マッチ開始インデックス
	 * @param endIndex
	 *            マッチ終了インデックス (exclusive)
	 */
	public MatchResult(ConversionRule rule, Map<Integer, List<String>> captures, int startIndex, int endIndex) {
		this.rule = Objects.requireNonNull(rule, "rule が null です");
		this.captures = Collections.unmodifiableMap(Objects.requireNonNull(captures, "captures が null です"));
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	/**
	 * マッチしたルールを返す。
	 *
	 * @return マッチルール
	 */
	public ConversionRule getRule() {
		return rule;
	}

	/**
	 * 全キャプチャ結果を返す。
	 *
	 * @return パラメータインデックス → トークンリストのマップ
	 */
	public Map<Integer, List<String>> getCaptures() {
		return captures;
	}

	/**
	 * 指定インデックスの ABSTRACT_PARAM にキャプチャされたトークンリストを返す。
	 *
	 * @param paramIndex
	 *            パラメータインデックス (0〜99)
	 * @return キャプチャされたトークンリスト (キャプチャなしの場合は空リスト)
	 */
	public List<String> getCapturedTokens(int paramIndex) {
		return captures.getOrDefault(paramIndex, List.of());
	}

	/**
	 * 指定インデックスの ABSTRACT_PARAM にキャプチャされたトークンを スペース区切りで結合した文字列を返す。
	 *
	 * @param paramIndex
	 *            パラメータインデックス (0〜99)
	 * @return スペース区切りのトークン文字列
	 */
	public String getCapturedText(int paramIndex) {
		List<String> tokens = getCapturedTokens(paramIndex);
		return String.join(" ", tokens);
	}

	/**
	 * マッチ開始インデックスを返す。
	 *
	 * @return 開始インデックス
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * マッチ終了インデックス (exclusive) を返す。
	 *
	 * @return 終了インデックス
	 */
	public int getEndIndex() {
		return endIndex;
	}

	/**
	 * マッチしたトークンの総数を返す。
	 *
	 * @return マッチトークン数
	 */
	public int getMatchLength() {
		return endIndex - startIndex;
	}

	/**
	 * ルールの to テンプレートを ABSTRACT_PARAM・RECEIVER のキャプチャで展開した文字列を返す。
	 *
	 * @return 展開後の置換テキスト
	 */
	public String getExpandedToTemplate() {
		return expandToTemplate(rule.getToTemplate(), captures);
	}

	/**
	 * テンプレート文字列を ABSTRACT_PARAM・RECEIVER のキャプチャで展開する（静的ユーティリティ）。
	 *
	 * @param template
	 *            テンプレート文字列
	 * @param captures
	 *            パラメータインデックス → トークンリストのマップ
	 * @return 展開後の文字列
	 */
	public static String expandToTemplate(String template, Map<Integer, List<String>> captures) {
		if (template == null)
			return "";
		String result = ABSTRACT_PARAM_PATTERN.matcher(template).replaceAll(mr -> Matcher
				.quoteReplacement(String.join(" ", captures.getOrDefault(Integer.parseInt(mr.group(1)), List.of()))));
		result = ABSTRACT_TOKEN_PATTERN.matcher(result).replaceAll(
				mr -> Matcher.quoteReplacement(String.join(" ", captures.getOrDefault(
						ConversionToken.ABSTRACT_TOKEN_CAPTURE_BASE + Integer.parseInt(mr.group(1)), List.of()))));
		List<String> receiverCapture = captures.getOrDefault(ConversionToken.RECEIVER_CAPTURE_KEY, List.of());
		result = RECEIVER_PATTERN.matcher(result)
				.replaceAll(Matcher.quoteReplacement(String.join(" ", receiverCapture)));
		return result;
	}

	@Override
	public String toString() {
		return String.format("MatchResult{rule=%s, start=%d, end=%d, captures=%s}", rule.getSourceFile(), startIndex,
				endIndex, captures);
	}
}
