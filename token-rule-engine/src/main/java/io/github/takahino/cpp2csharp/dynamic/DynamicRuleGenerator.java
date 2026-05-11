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

package io.github.takahino.cpp2csharp.dynamic;

import io.github.takahino.cpp2csharp.matcher.MatchResult;
import io.github.takahino.cpp2csharp.matcher.PatternMatcher;
import io.github.takahino.cpp2csharp.matcher.ReceiverValidator;
import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionToken;
import io.github.takahino.cpp2csharp.tree.AstNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 動的ルール生成クラス。
 *
 * <p>
 * {@link DynamicRuleSpec} の収集パターンをトークンストリームに適用し、 収集した値を {@code COLLECTED}
 * プレースホルダに代入して {@link ConversionRule} を動的に生成する。
 * </p>
 *
 * <h2>処理フロー</h2>
 * <ol>
 * <li>トークンノード列を文字列リストに変換</li>
 * <li>収集パターン ({@code collect:}) でトークンストリームを走査</li>
 * <li>ABSTRACT_PARAM00 キャプチャが単一トークン（識別子）のもののみ採用</li>
 * <li>収集値を重複排除</li>
 * <li>各収集値について、from/to テンプレートの {@code COLLECTED} を置換し ConversionRule を生成</li>
 * </ol>
 */
public class DynamicRuleGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRuleGenerator.class);

	/** COLLECTED プレースホルダの正規表現（単語境界付き） */
	private static final Pattern COLLECTED_PATTERN = Pattern.compile("\\bCOLLECTED\\b");

	private final ConversionRuleLoader ruleLoader;
	private final PatternMatcher patternMatcher;

	public DynamicRuleGenerator(ConversionRuleLoader ruleLoader) {
		this(ruleLoader, null);
	}

	public DynamicRuleGenerator(ConversionRuleLoader ruleLoader, ReceiverValidator receiverValidator) {
		this.ruleLoader = ruleLoader;
		this.patternMatcher = new PatternMatcher(receiverValidator);
	}

	/**
	 * 外部から提供された値リストと from/to テンプレートから ConversionRule を生成する。
	 *
	 * <p>
	 * VC++6 の enum メンバ名など、別処理（字句解析・AST解析）で収集した識別子を
	 * トークンストリームのスキャンなしに直接ルール化するためのエントリポイント。
	 * </p>
	 *
	 * <p>
	 * ユースケース: enum { apple, banana } のメンバ名を外部解析で取得し、
	 * {@code apple → (EnumType) apple} のようなキャストルールを一括生成する。
	 * </p>
	 *
	 * @param externalValues
	 *            外部から提供された識別子リスト（重複は除去しない）
	 * @param templates
	 *            from/to テンプレートのリスト（{@code COLLECTED} が各値に置換される）
	 * @param sourceName
	 *            ログ・ルール sourceFile 用の名称
	 * @return 生成された ConversionRule のリスト
	 */
	public List<ConversionRule> generateFromValues(List<String> externalValues,
			List<DynamicRuleSpec.FromToTemplate> templates, String sourceName) {
		if (externalValues.isEmpty() || templates.isEmpty()) {
			LOGGER.debug("外部値またはテンプレートが空のため動的ルールを生成しません: {}", sourceName);
			return List.of();
		}

		List<ConversionRule> rules = new ArrayList<>();
		for (String value : externalValues) {
			for (DynamicRuleSpec.FromToTemplate template : templates) {
				ConversionRule rule = instantiateRule(value, template, sourceName);
				if (rule != null) {
					rules.add(rule);
				}
			}
		}

		LOGGER.info("外部値から動的ルール生成 ({}): {} 値 × {} テンプレート → {} ルール", sourceName, externalValues.size(), templates.size(),
				rules.size());
		return rules;
	}

	/**
	 * トークンノード列と動的ルール仕様リストから ConversionRule を生成する。
	 *
	 * @param tokenNodes
	 *            変換対象のトークンノード列（PRE フェーズ完了後）
	 * @param dynamicSpecs
	 *            動的ルール仕様のリスト
	 * @return 生成された ConversionRule のリスト
	 */
	public List<ConversionRule> generate(List<AstNode> tokenNodes, List<DynamicRuleSpec> dynamicSpecs) {
		List<String> tokens = tokenNodes.stream().map(AstNode::getText).toList();
		List<ConversionRule> generatedRules = new ArrayList<>();

		for (DynamicRuleSpec spec : dynamicSpecs) {
			List<ConversionRule> rules = generateFromSpec(tokens, spec);
			generatedRules.addAll(rules);
		}

		LOGGER.info("動的ルール生成完了: 合計 {} ルール", generatedRules.size());
		return generatedRules;
	}

	/**
	 * 1つの {@link DynamicRuleSpec} から ConversionRule を生成する。
	 */
	private List<ConversionRule> generateFromSpec(List<String> tokens, DynamicRuleSpec spec) {
		// 収集パターンを ConversionRule として実行（to は使わない）
		ConversionRule collectRule = new ConversionRule(spec.sourceFile() + " [collect]", spec.collectPattern(), "" // to
																													// テンプレートは使用しない
		);

		List<MatchResult> matches = patternMatcher.matchAll(List.of(collectRule), tokens);

		// ABSTRACT_PARAM00 (index=0) の単一トークンキャプチャを収集（重複排除、順序保持）
		Set<String> collectedValues = new LinkedHashSet<>();
		for (MatchResult match : matches) {
			List<String> captured = match.getCapturedTokens(0);
			if (captured.size() == 1) {
				collectedValues.add(captured.get(0));
			}
		}

		if (collectedValues.isEmpty()) {
			LOGGER.debug("収集パターンにマッチする値がありません: {}", spec.sourceFile());
			return List.of();
		}

		LOGGER.info("動的ルール収集値 ({}): {}", spec.sourceFile(), collectedValues);

		// 各収集値 × 各テンプレートでルールを生成
		List<ConversionRule> rules = new ArrayList<>();
		for (String value : collectedValues) {
			for (DynamicRuleSpec.FromToTemplate template : spec.templates()) {
				ConversionRule rule = instantiateRule(value, template, spec.sourceFile());
				if (rule != null) {
					rules.add(rule);
				}
			}
		}

		return rules;
	}

	/**
	 * COLLECTED を具体値に置換して ConversionRule を生成する。
	 *
	 * @param value
	 *            収集した具体値（識別子）
	 * @param template
	 *            from/to テンプレート
	 * @param sourceFile
	 *            ソースファイル名
	 * @return 生成された ConversionRule（失敗時は null）
	 */
	private ConversionRule instantiateRule(String value, DynamicRuleSpec.FromToTemplate template, String sourceFile) {
		// from テンプレートの COLLECTED を具体値に置換してトークン化
		String instantiatedFrom = COLLECTED_PATTERN.matcher(template.fromTemplate()).replaceAll(value);
		List<ConversionToken> fromTokens;
		try {
			fromTokens = ruleLoader.tokenizePattern(instantiatedFrom);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("動的ルールの from パターンのトークン化に失敗: '{}' (value={}) - {}", instantiatedFrom, value, e.getMessage());
			return null;
		}

		// to テンプレートの COLLECTED を具体値に置換
		String instantiatedTo = COLLECTED_PATTERN.matcher(template.toTemplate()).replaceAll(value);

		LOGGER.debug("動的ルール生成: '{}' → '{}'", instantiatedFrom, instantiatedTo);
		return new ConversionRule(sourceFile + " [dynamic:" + value + "]", fromTokens, instantiatedTo);
	}
}
