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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 変換ルールにおける単一トークンを表すクラス。 具体的なリテラルトークン、ABSTRACT_PARAM[nn]、RECEIVER のいずれかである。
 *
 * <p>
 * ABSTRACT_PARAM[nn] は 00〜99 の番号付き抽象化パラメータで、 任意のトークン列にマッチする特殊トークンとして機能する。
 * </p>
 *
 * <p>
 * RECEIVER は postfix チェーン（識別子・メンバアクセス・添字・関数呼び出しの連鎖）
 * にマッチする役割別抽象化トークン。1ルールに1つのみ使用可能。captures マップでは {@link #RECEIVER_CAPTURE_KEY}
 * をキーとして格納される。
 * </p>
 *
 * <p>
 * REGEX トークン ({@code /pattern/} 形式) は正規表現によるマッチを行う特殊トークン。
 * </p>
 *
 * <p>
 * LEXER_TYPE トークン ({@code <TypeName>} 形式) はレキサートークン型名によるマッチを行う特殊トークン。
 * </p>
 */
public final class ConversionToken {

	/** ABSTRACT_PARAM の正規表現パターン (ABSTRACT_PARAM00 ～ ABSTRACT_PARAM99) */
	private static final Pattern ABSTRACT_PARAM_PATTERN = Pattern.compile("ABSTRACT_PARAM(\\d{2})");

	/** ABSTRACT_TOKEN の正規表現パターン (ABSTRACT_TOKEN00 ～ ABSTRACT_TOKEN99) */
	private static final Pattern ABSTRACT_TOKEN_PATTERN = Pattern.compile("ABSTRACT_TOKEN(\\d{2})");

	/** REGEX トークンのパターン: /pattern/ 形式 */
	private static final Pattern REGEX_TOKEN_PATTERN = Pattern.compile("^/(.+)/$");

	/** LEXER_TYPE トークンのパターン: <TypeName> 形式 */
	private static final Pattern LEXER_TYPE_TOKEN_PATTERN = Pattern.compile("^<([A-Za-z_][A-Za-z0-9_]*)>$");

	/** RECEIVER 抽象化トークンのキーワード文字列 */
	public static final String RECEIVER_TOKEN = "RECEIVER";

	/**
	 * RECEIVER キャプチャの captures マップ固定キー (値 = 100)。 ABSTRACT_PARAM (key 0-99) と衝突しない。
	 */
	public static final int RECEIVER_CAPTURE_KEY = 100;

	/**
	 * ABSTRACT_TOKEN キャプチャの captures マップ基底キー (値 = 200)。 ABSTRACT_TOKEN[nn] は
	 * {@code ABSTRACT_TOKEN_CAPTURE_BASE + paramIndex} (200-299) をキーとする。 ABSTRACT_PARAM (0-99)・
	 * RECEIVER (100) と衝突しない。
	 */
	public static final int ABSTRACT_TOKEN_CAPTURE_BASE = 200;

	/** トークンの文字列値 */
	private final String value;

	/** このトークンが ABSTRACT_PARAM 抽象化トークンであるか */
	private final boolean abstractParam;

	/** このトークンが ABSTRACT_TOKEN 抽象化トークンであるか */
	private final boolean abstractTokenParam;

	/** このトークンが RECEIVER 抽象化トークンであるか */
	private final boolean receiverParam;

	/** このトークンが REGEX トークンであるか */
	private final boolean regexParam;

	/** このトークンが LEXER_TYPE トークンであるか */
	private final boolean lexerTypeParam;

	/** 抽象化トークンのインデックス (0〜99)。通常トークンの場合は -1 */
	private final int paramIndex;

	/** REGEX トークンの場合の正規表現パターン文字列。非 REGEX の場合は null */
	private final String regexPattern;

	/** LEXER_TYPE トークンの場合のレキサー型名。非 LEXER_TYPE の場合は null */
	private final String lexerTypeName;

	/**
	 * プライベートコンストラクタ。{@link #of(String)} ファクトリメソッドを使用すること。
	 */
	private ConversionToken(String value, boolean abstractParam, boolean abstractTokenParam, boolean receiverParam,
			boolean regexParam, boolean lexerTypeParam, int paramIndex, String regexPattern, String lexerTypeName) {
		this.value = value;
		this.abstractParam = abstractParam;
		this.abstractTokenParam = abstractTokenParam;
		this.receiverParam = receiverParam;
		this.regexParam = regexParam;
		this.lexerTypeParam = lexerTypeParam;
		this.paramIndex = paramIndex;
		this.regexPattern = regexPattern;
		this.lexerTypeName = lexerTypeName;
	}

	/**
	 * 文字列からトークンを生成するファクトリメソッド。 ABSTRACT_PARAM[nn] 形式は抽象化トークン、ABSTRACT_TOKEN[nn] 形式は
	 * 単一トークン抽象化、"RECEIVER" はレシーバートークンとして生成する。 /pattern/ 形式は REGEX トークン、
	 * {@literal <TypeName>} 形式は LEXER_TYPE トークンとして生成する。
	 *
	 * @param value
	 *            トークン文字列
	 * @return 生成された ConversionToken
	 * @throws IllegalArgumentException
	 *             値が null または空の場合
	 */
	public static ConversionToken of(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("トークン値は空にできません");
		}
		Matcher m = ABSTRACT_PARAM_PATTERN.matcher(value);
		if (m.matches()) {
			int idx = Integer.parseInt(m.group(1));
			return new ConversionToken(value, true, false, false, false, false, idx, null, null);
		}
		Matcher tokenMatcher = ABSTRACT_TOKEN_PATTERN.matcher(value);
		if (tokenMatcher.matches()) {
			int idx = Integer.parseInt(tokenMatcher.group(1));
			return new ConversionToken(value, false, true, false, false, false, idx, null, null);
		}
		Matcher regexMatcher = REGEX_TOKEN_PATTERN.matcher(value);
		if (regexMatcher.matches()) {
			return new ConversionToken(value, false, false, false, true, false, -1, regexMatcher.group(1), null);
		}
		Matcher lexerTypeMatcher = LEXER_TYPE_TOKEN_PATTERN.matcher(value);
		if (lexerTypeMatcher.matches()) {
			return new ConversionToken(value, false, false, false, false, true, -1, null, lexerTypeMatcher.group(1));
		}
		if (RECEIVER_TOKEN.equals(value)) {
			return new ConversionToken(value, false, false, true, false, false, 0, null, null);
		}
		return new ConversionToken(value, false, false, false, false, false, -1, null, null);
	}

	/**
	 * トークンの文字列値を返す。
	 *
	 * @return トークン文字列
	 */
	public String getValue() {
		return value;
	}

	/**
	 * このトークンが ABSTRACT_PARAM かどうかを返す。
	 *
	 * @return ABSTRACT_PARAM トークンであれば true
	 */
	public boolean isAbstractParam() {
		return abstractParam;
	}

	/**
	 * このトークンが ABSTRACT_TOKEN（単一トークン抽象化）かどうかを返す。
	 *
	 * @return ABSTRACT_TOKEN トークンであれば true
	 */
	public boolean isAbstractTokenParam() {
		return abstractTokenParam;
	}

	/**
	 * このトークンが RECEIVER かどうかを返す。
	 *
	 * @return RECEIVER トークンであれば true
	 */
	public boolean isReceiverParam() {
		return receiverParam;
	}

	/**
	 * このトークンが REGEX トークン ({@code /pattern/} 形式) かどうかを返す。
	 *
	 * @return REGEX トークンであれば true
	 */
	public boolean isRegexParam() {
		return regexParam;
	}

	/**
	 * このトークンが LEXER_TYPE トークン ({@code <TypeName>} 形式) かどうかを返す。
	 *
	 * @return LEXER_TYPE トークンであれば true
	 */
	public boolean isLexerTypeParam() {
		return lexerTypeParam;
	}

	/**
	 * REGEX トークンの正規表現パターン文字列を返す。 非 REGEX トークンの場合は null を返す。
	 *
	 * @return 正規表現パターン文字列、または null
	 */
	public String getRegexPattern() {
		return regexPattern;
	}

	/**
	 * LEXER_TYPE トークンのレキサー型名を返す。 非 LEXER_TYPE トークンの場合は null を返す。
	 *
	 * @return レキサー型名、または null
	 */
	public String getLexerTypeName() {
		return lexerTypeName;
	}

	/**
	 * 抽象化トークンのインデックスを返す。 通常トークンの場合は -1 を返す。
	 *
	 * @return パラメータインデックス (0〜99)、または -1
	 */
	public int getParamIndex() {
		return paramIndex;
	}

	/**
	 * captures マップに使用するキーを返す。 ABSTRACT_PARAM: paramIndex (0-99) ABSTRACT_TOKEN:
	 * ABSTRACT_TOKEN_CAPTURE_BASE + paramIndex (200-299) RECEIVER: RECEIVER_CAPTURE_KEY
	 * (100、固定) 通常トークン: -1
	 *
	 * @return captures マップキー
	 */
	public int getCaptureKey() {
		if (abstractParam)
			return paramIndex;
		if (abstractTokenParam)
			return ABSTRACT_TOKEN_CAPTURE_BASE + paramIndex;
		if (receiverParam)
			return RECEIVER_CAPTURE_KEY;
		return -1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ConversionToken that))
			return false;
		return abstractParam == that.abstractParam && abstractTokenParam == that.abstractTokenParam
				&& receiverParam == that.receiverParam && regexParam == that.regexParam
				&& lexerTypeParam == that.lexerTypeParam && paramIndex == that.paramIndex
				&& Objects.equals(value, that.value) && Objects.equals(regexPattern, that.regexPattern)
				&& Objects.equals(lexerTypeName, that.lexerTypeName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, abstractParam, abstractTokenParam, receiverParam, regexParam, lexerTypeParam,
				paramIndex, regexPattern, lexerTypeName);
	}

	@Override
	public String toString() {
		if (abstractParam)
			return String.format("ABSTRACT_PARAM[%02d]", paramIndex);
		if (abstractTokenParam)
			return String.format("ABSTRACT_TOKEN[%02d]", paramIndex);
		if (receiverParam)
			return RECEIVER_TOKEN;
		if (regexParam)
			return "REGEX[" + regexPattern + "]";
		if (lexerTypeParam)
			return "LEXER_TYPE[" + lexerTypeName + "]";
		return value;
	}
}
