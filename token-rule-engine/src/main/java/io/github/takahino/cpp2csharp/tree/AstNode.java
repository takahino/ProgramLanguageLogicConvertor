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

package io.github.takahino.cpp2csharp.tree;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * パターンマッチング変換で使用するトークンノードクラス。
 *
 * <p>
 * ANTLR の TerminalNode に対応し、{@code text} は実際のトークン文字列を示す。
 * </p>
 */
public final class AstNode {

	private static final AtomicInteger idCounter = new AtomicInteger(0);

	/** ノードの一意識別子 */
	private final int id;

	/** トークンテキスト */
	private final String text;

	/** ソース位置情報 (行番号) */
	private final int line;

	/** ソース位置情報 (カラム番号) */
	private final int column;

	/**
	 * ANTLR トークンストリーム上のインデックス。 ストリーム情報がない場合（置換由来トークン）は -1。
	 */
	private final int streamIndex;

	/**
	 * トークンノードを生成するファクトリメソッド (ストリームインデックスなし)。
	 *
	 * @param text
	 *            トークンテキスト
	 * @param line
	 *            ソース行番号
	 * @param column
	 *            ソースカラム番号
	 * @return 生成されたトークンノード
	 */
	public static AstNode tokenNode(String text, int line, int column) {
		return new AstNode(text, line, column, -1);
	}

	/**
	 * トークンノードを生成するファクトリメソッド (ストリームインデックスあり)。
	 *
	 * @param text
	 *            トークンテキスト
	 * @param line
	 *            ソース行番号
	 * @param column
	 *            ソースカラム番号
	 * @param streamIndex
	 *            ANTLR トークンストリーム上のインデックス
	 * @return 生成されたトークンノード
	 */
	public static AstNode tokenNode(String text, int line, int column, int streamIndex) {
		return new AstNode(text, line, column, streamIndex);
	}

	/**
	 * 指定した ID でトークンノードを生成するファクトリメソッド。 木構造の直接修正（サブツリー置換）時に、置換位置を維持するために使用する。
	 *
	 * @param text
	 *            トークンテキスト
	 * @param line
	 *            ソース行番号
	 * @param column
	 *            ソースカラム番号
	 * @param id
	 *            ノードの一意識別子（既存ノードとの順序整合性のため指定）
	 * @return 生成されたトークンノード
	 */
	public static AstNode tokenNodeWithId(String text, int line, int column, int id) {
		return new AstNode(text, line, column, -1, id);
	}

	/**
	 * 指定した ID と streamIndex でトークンノードを生成するファクトリメソッド。 置換時に元トークンの streamIndex
	 * を継承し、直前の改行・空白を復元するために使用する。
	 *
	 * @param text
	 *            トークンテキスト
	 * @param line
	 *            ソース行番号
	 * @param column
	 *            ソースカラム番号
	 * @param id
	 *            ノードの一意識別子
	 * @param streamIndex
	 *            継承するストリームインデックス（-1 の場合は継承なし）
	 * @return 生成されたトークンノード
	 */
	public static AstNode tokenNodeWithId(String text, int line, int column, int id, int streamIndex) {
		return new AstNode(text, line, column, streamIndex, id);
	}

	private AstNode(String text, int line, int column, int streamIndex) {
		this(text, line, column, streamIndex, idCounter.getAndIncrement());
	}

	private AstNode(String text, int line, int column, int streamIndex, int id) {
		this.id = id;
		idCounter.updateAndGet(current -> Math.max(current, id + 1));
		this.text = Objects.requireNonNull(text);
		this.line = line;
		this.column = column;
		this.streamIndex = streamIndex;
	}

	/**
	 * ノードの一意識別子を返す。
	 *
	 * @return ノード ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * テキスト (トークン文字列) を返す。
	 *
	 * @return テキスト
	 */
	public String getText() {
		return text;
	}

	/**
	 * ソース行番号を返す。
	 *
	 * @return 行番号
	 */
	public int getLine() {
		return line;
	}

	/**
	 * ソースカラム番号を返す。
	 *
	 * @return カラム番号
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * ANTLR トークンストリーム上のインデックスを返す。 ストリーム情報がない場合は -1 を返す。
	 *
	 * @return ストリームインデックス、または -1
	 */
	public int getStreamIndex() {
		return streamIndex;
	}

	@Override
	public String toString() {
		return String.format("AstNode{id=%d, text='%s', pos=%d:%d}", id, text, line, column);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AstNode that))
			return false;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Integer.hashCode(id);
	}
}
