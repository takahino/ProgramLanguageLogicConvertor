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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 単一の変換ルールを表すクラス。 from トークン列と to テンプレート文字列で構成される。 オプションで test/assrt
 * ペアによるルール内蔵テストを保持できる。
 *
 * <p>
 * 例:
 * </p>
 *
 * <pre>
 * from : this.AfxMessageBox(ABSTRACT_PARAM00, MB_OK | MB_ICONERROR);
 * to : MessageBox.Show(ABSTRACT_PARAM00, "", MessageBoxButtons.OK, MessageBoxIcon.Error);
 * test : AfxMessageBox("Hello", MB_OK | MB_ICONERROR);
 * assrt : MessageBox.Show("Hello", "", MessageBoxButtons.OK, MessageBoxIcon.Error);
 * </pre>
 */
public final class ConversionRule {

	/** このルールを定義しているファイル名 (デバッグ用) */
	private final String sourceFile;

	/** from パターンのトークン列 */
	private final List<ConversionToken> fromTokens;

	/** to テンプレート文字列 */
	private final String toTemplate;

	/** ルール内蔵テスト (test/assrt ペアのリスト) */
	private final List<RuleTestCase> testCases;

	/**
	 * コンストラクタ（テストケースなし）。
	 *
	 * @param sourceFile
	 *            定義元ファイル名
	 * @param fromTokens
	 *            from パターンのトークン列
	 * @param toTemplate
	 *            to テンプレート文字列
	 */
	public ConversionRule(String sourceFile, List<ConversionToken> fromTokens, String toTemplate) {
		this(sourceFile, fromTokens, toTemplate, List.of());
	}

	/**
	 * コンストラクタ。
	 *
	 * @param sourceFile
	 *            定義元ファイル名
	 * @param fromTokens
	 *            from パターンのトークン列
	 * @param toTemplate
	 *            to テンプレート文字列
	 * @param testCases
	 *            ルール内蔵テスト (test/assrt ペア)
	 */
	public ConversionRule(String sourceFile, List<ConversionToken> fromTokens, String toTemplate,
			List<RuleTestCase> testCases) {
		this.sourceFile = Objects.requireNonNull(sourceFile, "sourceFile が null です");
		this.fromTokens = Collections.unmodifiableList(Objects.requireNonNull(fromTokens, "fromTokens が null です"));
		this.toTemplate = Objects.requireNonNull(toTemplate, "toTemplate が null です");
		this.testCases = Collections.unmodifiableList(testCases != null ? testCases : List.of());
	}

	/**
	 * 定義元ファイル名を返す。
	 *
	 * @return ファイル名
	 */
	public String getSourceFile() {
		return sourceFile;
	}

	/**
	 * from パターンのトークン列を返す。
	 *
	 * @return 不変のトークンリスト
	 */
	public List<ConversionToken> getFromTokens() {
		return fromTokens;
	}

	/**
	 * to テンプレート文字列を返す。
	 *
	 * @return to テンプレート
	 */
	public String getToTemplate() {
		return toTemplate;
	}

	/**
	 * ルール内蔵テスト (test/assrt ペア) のリストを返す。
	 *
	 * @return 不変のテストケースリスト（空の場合は空リスト）
	 */
	public List<RuleTestCase> getTestCases() {
		return testCases;
	}

	/**
	 * from パターンに含まれる ABSTRACT_PARAM の最大インデックス数を返す。
	 *
	 * @return 使用している ABSTRACT_PARAM の個数
	 */
	public long getAbstractParamCount() {
		return fromTokens.stream().filter(ConversionToken::isAbstractParam).map(ConversionToken::getParamIndex)
				.distinct().count();
	}

	/**
	 * from パターンから期待する引数個数を導出する。
	 *
	 * <p>
	 * ANTLR CPP14 文法で構文解析し、最初の関数呼び出しの expressionList から initializerClause
	 * 数を取得する。文字列操作ではなく構文解析により正確に判定する。
	 * </p>
	 *
	 * <p>
	 * 括弧を含まないパターン（型変換など）は -1 を返し、引数個数フィルタの対象外とする。
	 * </p>
	 *
	 * @return 期待する引数個数、または -1（フィルタ対象外）
	 */
	public int getArgumentCount() {
		return RulePatternParser.parseArgumentCount(fromTokens);
	}

	@Override
	public String toString() {
		return String.format("ConversionRule{from=%s, to='%s', file='%s'}", fromTokens, toTemplate, sourceFile);
	}
}
