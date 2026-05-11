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

import io.github.takahino.cpp2csharp.transform.AppliedTransform;
import io.github.takahino.cpp2csharp.transform.DiagnosticCandidate;
import io.github.takahino.cpp2csharp.transform.Transformer.TransformError;

import java.util.List;

/**
 * C++ → C# 変換の結果を保持するクラス。
 *
 * <p>
 * 変換後の C# コード、変換エラー情報、パースエラー情報、診断候補を保持する。
 * </p>
 */
public final class ConversionResult {

	/** 変換後の C# コード文字列 */
	private final String csCode;

	/** 変換エラーリスト (曖昧マッチなど) */
	private final List<TransformError> transformErrors;

	/** ANTLR パースエラーリスト */
	private final List<String> parseErrors;

	/** 変換前 AST の木ダンプ（ルール設計デバッグ用、null の場合は未生成） */
	private final String initialTreeDump;

	/** 適用した変換のログ（時系列、レポート出力用） */
	private final List<AppliedTransform> appliedTransforms;

	/** 診断候補リスト（フィルタ無視再マッチで検出した要確認候補） */
	private final List<DiagnosticCandidate> diagnosticCandidates;

	/** PRE/POST/COMBY フェーズの適用ログ */
	private final List<PhaseTransformLog> phaseTransformLogs;

	/** フェーズ変換ジャーニースナップショット（phases.html 用） */
	private final List<PhaseSnapshot> phaseSnapshots;

	/** MAIN フェーズ入力のユニット別ソーステキスト（デバッグ用、空リストは出力なし） */
	private final List<String> unitSourceDumps;

	/** MAIN フェーズ全ユニットの変換後テキスト（_units/basename_N.cs.txt 出力用） */
	private final List<String> unitOutputDumps;

	/** body ユニットのみの関数単位エントリ（basename.json 出力用） */
	private final List<FunctionUnitEntry> functionUnitEntries;

	/**
	 * コンストラクタ（後方互換）。診断候補・フェーズログは空リストで初期化する。
	 */
	public ConversionResult(String csCode, List<TransformError> transformErrors, List<String> parseErrors,
			String initialTreeDump, List<AppliedTransform> appliedTransforms) {
		this(csCode, transformErrors, parseErrors, initialTreeDump, appliedTransforms, null, List.of(), List.of());
	}

	/**
	 * コンストラクタ。
	 *
	 * @param csCode
	 *            変換後の C# コード
	 * @param transformErrors
	 *            変換エラーリスト
	 * @param parseErrors
	 *            パースエラーリスト
	 * @param initialTreeDump
	 *            変換前 AST の木ダンプ（null 可）
	 * @param appliedTransforms
	 *            適用した変換のログ（null の場合は空リスト）
	 * @param diagnosticCandidates
	 *            診断候補リスト（null の場合は空リスト）
	 */
	public ConversionResult(String csCode, List<TransformError> transformErrors, List<String> parseErrors,
			String initialTreeDump, List<AppliedTransform> appliedTransforms,
			List<DiagnosticCandidate> diagnosticCandidates) {
		this(csCode, transformErrors, parseErrors, initialTreeDump, appliedTransforms, diagnosticCandidates, List.of(),
				List.of());
	}

	/**
	 * フルコンストラクタ（フェーズ変換ログ付き）。
	 *
	 * @param phaseTransformLogs
	 *            PRE/POST/COMBY フェーズ適用ログ
	 */
	public ConversionResult(String csCode, List<TransformError> transformErrors, List<String> parseErrors,
			String initialTreeDump, List<AppliedTransform> appliedTransforms,
			List<DiagnosticCandidate> diagnosticCandidates, List<PhaseTransformLog> phaseTransformLogs) {
		this(csCode, transformErrors, parseErrors, initialTreeDump, appliedTransforms, diagnosticCandidates,
				phaseTransformLogs, List.of());
	}

	/**
	 * フルコンストラクタ（フェーズ変換ジャーニースナップショット付き）。
	 *
	 * @param phaseTransformLogs
	 *            PRE/POST/COMBY フェーズ適用ログ
	 * @param phaseSnapshots
	 *            フェーズ変換ジャーニースナップショット（phases.html 用）
	 */
	public ConversionResult(String csCode, List<TransformError> transformErrors, List<String> parseErrors,
			String initialTreeDump, List<AppliedTransform> appliedTransforms,
			List<DiagnosticCandidate> diagnosticCandidates, List<PhaseTransformLog> phaseTransformLogs,
			List<PhaseSnapshot> phaseSnapshots) {
		this(csCode, transformErrors, parseErrors, initialTreeDump, appliedTransforms, diagnosticCandidates,
				phaseTransformLogs, phaseSnapshots, List.of());
	}

	/**
	 * フルコンストラクタ（ユニット分割デバッグダンプ付き）。委譲用。
	 *
	 * @param unitSourceDumps
	 *            MAIN フェーズ入力ユニット別ソーステキスト（デバッグ用）
	 */
	public ConversionResult(String csCode, List<TransformError> transformErrors, List<String> parseErrors,
			String initialTreeDump, List<AppliedTransform> appliedTransforms,
			List<DiagnosticCandidate> diagnosticCandidates, List<PhaseTransformLog> phaseTransformLogs,
			List<PhaseSnapshot> phaseSnapshots, List<String> unitSourceDumps) {
		this(csCode, transformErrors, parseErrors, initialTreeDump, appliedTransforms, diagnosticCandidates,
				phaseTransformLogs, phaseSnapshots, unitSourceDumps, List.of(), List.of());
	}

	/**
	 * フルコンストラクタ（変換後ユニットダンプ・JSON エントリ付き）。
	 *
	 * @param unitSourceDumps
	 *            MAIN フェーズ入力ユニット別ソーステキスト（デバッグ用）
	 * @param unitOutputDumps
	 *            MAIN フェーズ全ユニット変換後テキスト（_units/basename_N.cs.txt 用）
	 * @param functionUnitEntries
	 *            body ユニットのみの関数単位エントリ（basename.json 用）
	 */
	public ConversionResult(String csCode, List<TransformError> transformErrors, List<String> parseErrors,
			String initialTreeDump, List<AppliedTransform> appliedTransforms,
			List<DiagnosticCandidate> diagnosticCandidates, List<PhaseTransformLog> phaseTransformLogs,
			List<PhaseSnapshot> phaseSnapshots, List<String> unitSourceDumps, List<String> unitOutputDumps,
			List<FunctionUnitEntry> functionUnitEntries) {
		this.csCode = csCode;
		this.transformErrors = List.copyOf(transformErrors);
		this.parseErrors = List.copyOf(parseErrors);
		this.initialTreeDump = initialTreeDump;
		this.appliedTransforms = appliedTransforms != null ? List.copyOf(appliedTransforms) : List.of();
		this.diagnosticCandidates = diagnosticCandidates != null ? List.copyOf(diagnosticCandidates) : List.of();
		this.phaseTransformLogs = phaseTransformLogs != null ? List.copyOf(phaseTransformLogs) : List.of();
		this.phaseSnapshots = phaseSnapshots != null ? List.copyOf(phaseSnapshots) : List.of();
		this.unitSourceDumps = unitSourceDumps != null ? List.copyOf(unitSourceDumps) : List.of();
		this.unitOutputDumps = unitOutputDumps != null ? List.copyOf(unitOutputDumps) : List.of();
		this.functionUnitEntries = functionUnitEntries != null ? List.copyOf(functionUnitEntries) : List.of();
	}

	/**
	 * PRE/POST/COMBY フェーズの適用ログを返す。
	 *
	 * @return フェーズ適用ログリスト
	 */
	public List<PhaseTransformLog> getPhaseTransformLogs() {
		return phaseTransformLogs;
	}

	/**
	 * フェーズ変換ジャーニースナップショットを返す（phases.html 用）。
	 *
	 * @return スナップショットリスト
	 */
	public List<PhaseSnapshot> getPhaseSnapshots() {
		return phaseSnapshots;
	}

	/**
	 * 変換前 AST の木ダンプを返す。
	 *
	 * @return 木ダンプ文字列。未生成の場合は null
	 */
	public String getInitialTreeDump() {
		return initialTreeDump;
	}

	/**
	 * 適用した変換のログを時系列で返す。
	 *
	 * @return 適用ログ（レポート出力用）
	 */
	public List<AppliedTransform> getAppliedTransforms() {
		return appliedTransforms;
	}

	/**
	 * 診断候補リストを返す。
	 *
	 * @return 診断候補（フィルタ無視再マッチで検出した要確認候補）
	 */
	public List<DiagnosticCandidate> getDiagnosticCandidates() {
		return diagnosticCandidates;
	}

	/**
	 * 変換後の C# コード文字列を返す。
	 *
	 * @return C# コード文字列
	 */
	public String getCsCode() {
		return csCode;
	}

	/**
	 * 変換エラーリストを返す。空の場合はエラーなし。
	 *
	 * @return 変換エラーリスト
	 */
	public List<TransformError> getTransformErrors() {
		return transformErrors;
	}

	/**
	 * ANTLR パースエラーリストを返す。空の場合はパースエラーなし。
	 *
	 * @return パースエラーリスト
	 */
	public List<String> getParseErrors() {
		return parseErrors;
	}

	/**
	 * MAIN フェーズ入力ユニット別ソーステキストを返す（デバッグ用）。 空リストの場合はユニットダンプが生成されていない。
	 *
	 * @return ユニット別ソーステキストリスト（1始まり連番に対応）
	 */
	public List<String> getUnitSourceDumps() {
		return unitSourceDumps;
	}

	/**
	 * MAIN フェーズ全ユニットの変換後テキストを返す（_units/basename_N.cs.txt 出力用）。 空リストの場合は出力なし。
	 *
	 * @return 全ユニット変換後テキストリスト（N 番号は .cpp.txt と同一）
	 */
	public List<String> getUnitOutputDumps() {
		return unitOutputDumps;
	}

	/**
	 * body ユニットのみの関数単位エントリを返す（basename.json 出力用）。 空リストの場合は出力なし。
	 *
	 * @return FunctionUnitEntry リスト
	 */
	public List<FunctionUnitEntry> getFunctionUnitEntries() {
		return functionUnitEntries;
	}

	/**
	 * 変換が成功したかどうかを返す。 パースエラーおよび変換エラーがともに0件の場合に成功とみなす。
	 *
	 * @return エラーがなければ true
	 */
	public boolean isSuccess() {
		return parseErrors.isEmpty() && transformErrors.isEmpty();
	}

	@Override
	public String toString() {
		return String.format("ConversionResult{parseErrors=%d, transformErrors=%d, code='%s'}", parseErrors.size(),
				transformErrors.size(), csCode.length() > 80 ? csCode.substring(0, 80) + "..." : csCode);
	}
}
