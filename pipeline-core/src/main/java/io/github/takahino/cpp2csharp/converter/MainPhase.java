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

import io.github.takahino.cpp2csharp.dynamic.DynamicRuleGenerator;
import io.github.takahino.cpp2csharp.dynamic.DynamicRuleSpec;
import io.github.takahino.cpp2csharp.matcher.ReceiverValidator;
import io.github.takahino.cpp2csharp.retokenize.RetokenizeResult;
import io.github.takahino.cpp2csharp.retokenize.Retokenizer;
import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.LanguageLexerFactory;
import io.github.takahino.cpp2csharp.rule.MainPhaseSubPhase;
import io.github.takahino.cpp2csharp.transform.Transformer;
import io.github.takahino.cpp2csharp.tree.AstNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MAIN フェーズの実装。静的ルール（{@code .rule}）と動的ルール（{@code .drule}）を適用する。
 *
 * <h2>関数単位処理</h2>
 * <p>
 * トークン列を {@link FunctionUnitSplitter} で関数定義単位に分割し、 各単位に対して変換を実行する。 単位分割により 1
 * パスあたりの探索トークン数が減少し処理を高速化する。
 * </p>
 *
 * <h2>state 管理と Transformer の使い方</h2>
 * <p>
 * 複数ユニットにわたって appliedTransforms / errors が正しく累積されるよう、
 * {@link Transformer#prepareForNewConversion} を 1 回だけ呼んだ後、 ユニットごとに
 * {@link Transformer#processUnitReturnNodes} を呼ぶ。 全ユニット完了後に
 * {@link Transformer#runPostTransformScans} で 診断スキャンとニアミススキャンを実行する。
 * </p>
 *
 * <p>
 * {@link Transformer} インスタンスは {@code CppToCSharpConverter} と共有される。 パイプライン終了後も
 * {@code transformer.getErrors()} 等を読み続けるため、 converter が所有し続ける。
 * </p>
 */
public class MainPhase implements ConversionPhase {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainPhase.class);

	private final List<MainPhaseSubPhase> mainPhaseSpecs;
	private final List<DynamicRuleSpec> dynamicSpecs;
	private final Transformer transformer;
	private final ConversionRuleLoader ruleLoader;
	/**
	 * ParseTree から抽出した関数定義の stream index 範囲リスト。 各 {@code int[2]} =
	 * [startStreamIndex, stopStreamIndex]。 空リストの場合は
	 * {@link FunctionUnitSplitter#split(List)} のブラケット深度方式にフォールバックする。
	 */
	private final List<int[]> functionDefinitionRanges;
	private final ReceiverValidator receiverValidator;
	private final Retokenizer retokenizer;

	/**
	 * @param mainPhaseSpecs
	 *            静的 MAIN フェーズのサブフェーズ仕様群
	 * @param dynamicSpecs
	 *            動的ルール仕様（空リストの場合は動的生成しない）
	 * @param transformer
	 *            変換エンジン（converter と共有）
	 * @param ruleLoader
	 *            動的ルール生成に使用するルールローダー
	 * @param functionDefinitionRanges
	 *            ParseTree から抽出した関数定義範囲（空リストでフォールバック）
	 */
	public MainPhase(List<MainPhaseSubPhase> mainPhaseSpecs, List<DynamicRuleSpec> dynamicSpecs,
			Transformer transformer, ConversionRuleLoader ruleLoader, List<int[]> functionDefinitionRanges) {
		this(mainPhaseSpecs, dynamicSpecs, transformer, ruleLoader, functionDefinitionRanges, null, null);
	}

	/**
	 * @param mainPhaseSpecs
	 *            静的 MAIN フェーズのサブフェーズ仕様群
	 * @param dynamicSpecs
	 *            動的ルール仕様（空リストの場合は動的生成しない）
	 * @param transformer
	 *            変換エンジン（converter と共有）
	 * @param ruleLoader
	 *            動的ルール生成に使用するルールローダー
	 * @param functionDefinitionRanges
	 *            ParseTree から抽出した関数定義範囲（空リストでフォールバック）
	 * @param receiverValidator
	 *            RECEIVER キャプチャの妥当性検証器（null の場合はプリフィルタのみ使用）
	 */
	public MainPhase(List<MainPhaseSubPhase> mainPhaseSpecs, List<DynamicRuleSpec> dynamicSpecs,
			Transformer transformer, ConversionRuleLoader ruleLoader, List<int[]> functionDefinitionRanges,
			ReceiverValidator receiverValidator) {
		this(mainPhaseSpecs, dynamicSpecs, transformer, ruleLoader, functionDefinitionRanges, receiverValidator, null);
	}

	/**
	 * @param mainPhaseSpecs
	 *            静的 MAIN フェーズのサブフェーズ仕様群
	 * @param dynamicSpecs
	 *            動的ルール仕様（空リストの場合は動的生成しない）
	 * @param transformer
	 *            変換エンジン（converter と共有）
	 * @param ruleLoader
	 *            動的ルール生成に使用するルールローダー
	 * @param functionDefinitionRanges
	 *            ParseTree から抽出した関数定義範囲（空リストでフォールバック）
	 * @param receiverValidator
	 *            RECEIVER キャプチャの妥当性検証器（null の場合はプリフィルタのみ使用）
	 * @param lexerFactory
	 *            MAINサブフェーズ間の再トークン化に使用するファクトリ（null の場合は再トークン化を行わない）
	 */
	public MainPhase(List<MainPhaseSubPhase> mainPhaseSpecs, List<DynamicRuleSpec> dynamicSpecs,
			Transformer transformer, ConversionRuleLoader ruleLoader, List<int[]> functionDefinitionRanges,
			ReceiverValidator receiverValidator, LanguageLexerFactory lexerFactory) {
		this.mainPhaseSpecs = mainPhaseSpecs;
		this.dynamicSpecs = dynamicSpecs;
		this.transformer = transformer;
		this.ruleLoader = ruleLoader;
		this.functionDefinitionRanges = functionDefinitionRanges;
		this.receiverValidator = receiverValidator;
		this.retokenizer = new Retokenizer(lexerFactory);
	}

	@Override
	public String name() {
		return "MAIN";
	}

	@Override
	public PhaseExecutionResult execute(PhaseExecutionContext ctx) {
		List<MainPhaseSubPhase> effectiveMainPhaseSpecs = new ArrayList<>(mainPhaseSpecs);

		// 動的ルール生成（PRE 完了後のトークンストリーム全体から収集）
		// collect: パターンはファイル全体のトークンを対象とするため、単位分割前に実行する
		if (!dynamicSpecs.isEmpty()) {
			DynamicRuleGenerator dynamicGenerator = new DynamicRuleGenerator(ruleLoader, receiverValidator);
			List<ConversionRule> dynamicRules = dynamicGenerator.generate(ctx.tokenNodes(), dynamicSpecs);
			if (!dynamicRules.isEmpty()) {
				LOGGER.info("動的ルール追加: {} ルール (MAIN フェーズ末尾に追加)", dynamicRules.size());
				// 動的ルールは最後のフェーズに追加する。retokenizeAfter=false（最後なので不問だがデフォルト値に合わせる）
				effectiveMainPhaseSpecs.add(new MainPhaseSubPhase("dynamic", dynamicRules, false));
			}
		}

		List<AstNode> mainResult;
		List<PhaseSnapshot> snapshots = new ArrayList<>();

		if (!effectiveMainPhaseSpecs.isEmpty()) {
			LOGGER.info("MAIN フェーズ開始: {} サブフェーズ (ParseTree範囲={})", effectiveMainPhaseSpecs.size(),
					functionDefinitionRanges.isEmpty() ? "なし(全体1単位)" : functionDefinitionRanges.size() + "件");

			// state を 1 回だけリセット（ユニットをまたいで appliedTransforms 等を累積させる）
			transformer.prepareForNewConversion();

			List<AstNode> currentTokenNodes = ctx.tokenNodes();
			Map<Integer, List<String>> currentComments = ctx.commentsBeforeToken();
			List<int[]> currentRanges = functionDefinitionRanges;

			List<String> unitOutputDumps = new ArrayList<>();
			List<UnitLabel> unitLabels = new ArrayList<>();

			for (int phaseIdx = 0; phaseIdx < effectiveMainPhaseSpecs.size(); phaseIdx++) {
				MainPhaseSubPhase spec = effectiveMainPhaseSpecs.get(phaseIdx);
				List<ConversionRule> subPhaseRules = spec.rules();
				boolean isLast = (phaseIdx == effectiveMainPhaseSpecs.size() - 1);

				List<TokenUnit> units = FunctionUnitSplitter.split(currentTokenNodes, currentRanges);
				LOGGER.info("MAIN サブフェーズ {}/{}: {} 単位", phaseIdx + 1, effectiveMainPhaseSpecs.size(), units.size());

				List<AstNode> phaseResult = new ArrayList<>();
				List<String> phaseUnitOutputDumps = new ArrayList<>();
				List<UnitLabel> phaseUnitLabels = new ArrayList<>();

				for (TokenUnit unit : units) {
					if (unit.tokens().isEmpty()) {
						continue;
					}
					List<AstNode> unitResult = transformer.processUnitReturnNodes(unit.tokens(), List.of(subPhaseRules),
							currentComments);
					phaseResult.addAll(unitResult);
					// 全ユニット（gap/body）の変換後テキストを収集
					phaseUnitOutputDumps.add(transformer.buildOutput(unitResult, currentComments));
					phaseUnitLabels.add(unit.label());
				}

				currentTokenNodes = phaseResult;
				// 最後のサブフェーズの unitOutputDumps/unitLabels を保持（デバッグファイル出力は最終状態を反映）
				unitOutputDumps = phaseUnitOutputDumps;
				unitLabels = phaseUnitLabels;

				String phaseCode = transformer.buildOutput(currentTokenNodes, currentComments);
				snapshots.add(new PhaseSnapshot("MAIN-" + (phaseIdx + 1), phaseCode));

				// デフォルトは再トークン化しない（安全側）。
				//
				// 再トークン化をスキップする理由（デフォルト）:
				// 再分解により、前フェーズで C# として確定した箇所が再び C++ トークン列として
				// 扱われ、後続フェーズのルールが意図せず当たるリスクがある。
				//
				// 再トークン化が必要な場合（フォルダ名に "+after_retok" を付ける）:
				// splice による置換結果は 1 つの合成ノードとして格納される。
				// 次サブフェーズが合成ノード内の個々のトークン（例: "Math", ".", "Sin"）を
				// パターンとして認識させたい場合は、合成ノードを再分解する必要がある。
				// フォルダ名に "+after_retok" を付けることで明示的に有効化できる。
				if (!isLast && spec.retokenizeAfter()) {
					RetokenizeResult retokenized = retokenizer.retokenize(currentTokenNodes, currentComments);
					LOGGER.info("MAIN サブフェーズ {} 後 再トークン化: {} トークン (フォルダ: {})", phaseIdx + 1,
							retokenized.tokenNodes().size(), spec.dirName());
					currentTokenNodes = retokenized.tokenNodes();
					currentComments = retokenized.commentsBeforeToken();
					// 再トークン化後は stream index が再採番されるため元の範囲は無効
					currentRanges = List.of();
				}
			}

			mainResult = currentTokenNodes;

			// 全ユニット完了後: 結合結果に対して診断・ニアミススキャンを 1 回実行
			List<List<ConversionRule>> allRules = effectiveMainPhaseSpecs.stream().map(MainPhaseSubPhase::rules)
					.toList();
			transformer.runPostTransformScans(mainResult, allRules);

			String code = transformer.buildOutput(mainResult, currentComments);
			LOGGER.info("MAIN フェーズ完了: {} トークン", mainResult.size());

			// logs は空（appliedTransforms は transformer フィールド経由で converter が収集）
			return new PhaseExecutionResult(mainResult, currentComments, code, snapshots, List.of(), unitOutputDumps,
					unitLabels);
		} else {
			// MAIN ルールが空の場合も Transformer の state をリセットする
			transformer.prepareForNewConversion();
			mainResult = ctx.tokenNodes();
		}

		String code = transformer.buildOutput(mainResult, ctx.commentsBeforeToken());
		return new PhaseExecutionResult(mainResult, ctx.commentsBeforeToken(), code, snapshots, List.of(), List.of(),
				List.of());
	}
}
