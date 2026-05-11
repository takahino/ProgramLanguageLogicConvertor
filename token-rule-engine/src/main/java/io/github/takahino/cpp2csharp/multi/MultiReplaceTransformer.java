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

package io.github.takahino.cpp2csharp.multi;

import io.github.takahino.cpp2csharp.converter.PhaseTransformLog;
import io.github.takahino.cpp2csharp.matcher.MatchResult;
import io.github.takahino.cpp2csharp.mrule.MRuleFindSpec;
import io.github.takahino.cpp2csharp.mrule.MultiReplaceRule;
import io.github.takahino.cpp2csharp.rule.ConversionToken;
import io.github.takahino.cpp2csharp.tree.AstNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * マルチ置換ルールをトークンノード列に適用する変換器クラス。
 *
 * <h2>変換ループ</h2>
 * <p>
 * 1フェーズ内で全ルールのマッチがなくなるまで繰り返し適用する。 各パスで1ルールを適用し、適用済みルールIDは記録して再適用を防ぐ。
 * </p>
 */
public class MultiReplaceTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiReplaceTransformer.class);
	private static final int MAX_PASSES = 50_000;

	private final MultiReplaceMatcher matcher = new MultiReplaceMatcher();

	/** フェーズ適用ログ（フェーズ実行後に getLogs() で取得） */
	private final List<PhaseTransformLog> logs = new ArrayList<>();

	/**
	 * 直近フェーズの適用ログを返す。
	 *
	 * @return 適用ログリスト（読み取り専用コピー）
	 */
	public List<PhaseTransformLog> getLogs() {
		return List.copyOf(logs);
	}

	/**
	 * ログをクリアする。
	 */
	public void clearLogs() {
		logs.clear();
	}

	/**
	 * マルチ置換ルールの1フェーズ分を適用する（後方互換オーバーロード）。
	 *
	 * @param tokenNodes
	 *            入力トークンノード列
	 * @param rules
	 *            適用するルールリスト
	 * @return 変換後のトークンノード列
	 */
	public List<AstNode> transformPhase(List<AstNode> tokenNodes, List<MultiReplaceRule> rules) {
		return transformPhase(tokenNodes, rules, "MRULE", 0);
	}

	/**
	 * マルチ置換ルールの1フェーズ分を適用する。
	 *
	 * @param tokenNodes
	 *            入力トークンノード列
	 * @param rules
	 *            適用するルールリスト
	 * @param phaseName
	 *            フェーズ名（"PRE", "POST" 等）
	 * @param phaseIndex
	 *            フェーズ番号（1始まり）
	 * @return 変換後のトークンノード列
	 */
	public List<AstNode> transformPhase(List<AstNode> tokenNodes, List<MultiReplaceRule> rules, String phaseName,
			int phaseIndex) {
		List<AstNode> current = new ArrayList<>(tokenNodes);

		for (int pass = 0; pass < MAX_PASSES; pass++) {
			List<MultiReplaceMatchResult> allMatches = matcher.matchAll(rules, current);
			if (allMatches.isEmpty())
				break;
			List<AstNode> next = applyAtomically(current, allMatches.get(0), phaseName, phaseIndex);
			if (textsEqual(current, next))
				break; // 置換前後が同一 → 無限ループ防止
			current = next;
		}
		return current;
	}

	private static boolean textsEqual(List<AstNode> a, List<AstNode> b) {
		if (a.size() != b.size())
			return false;
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).getText().equals(b.get(i).getText()))
				return false;
		}
		return true;
	}

	private List<AstNode> applyAtomically(List<AstNode> tokenNodes, MultiReplaceMatchResult match, String phaseName,
			int phaseIndex) {
		List<AstNode> result = new ArrayList<>(tokenNodes);

		List<MatchResult> stepMatches = match.stepMatches();
		List<MRuleFindSpec> specs = match.rule().getFindSpecs();
		Map<Integer, List<String>> captures = match.captures();

		// Sort steps by startIndex descending for safe in-place deletion
		List<int[]> stepIndices = new ArrayList<>();
		for (int i = 0; i < stepMatches.size(); i++) {
			MatchResult m = stepMatches.get(i);
			stepIndices.add(new int[]{m.getStartIndex(), m.getEndIndex(), i});
		}
		stepIndices.sort((a, b) -> Integer.compare(b[0], a[0]));

		// Collect matched text before any splicing (ascending order for readability)
		StringBuilder matchedTextBuilder = new StringBuilder();
		List<int[]> ascendingIndices = new ArrayList<>(stepIndices);
		ascendingIndices.sort((a, b) -> Integer.compare(a[0], b[0]));
		for (int[] si : ascendingIndices) {
			int s = si[0], e = si[1];
			if (s < tokenNodes.size() && e <= tokenNodes.size()) {
				if (matchedTextBuilder.length() > 0)
					matchedTextBuilder.append(" ... ");
				matchedTextBuilder.append(
						tokenNodes.subList(s, e).stream().map(AstNode::getText).collect(Collectors.joining(" ")));
			}
		}

		// Collect replacement texts and from/to for logging
		StringBuilder replacedTextBuilder = new StringBuilder();
		StringBuilder fromPatternBuilder = new StringBuilder();
		StringBuilder toPatternBuilder = new StringBuilder();
		for (int[] si : ascendingIndices) {
			int specIndex = si[2];
			MRuleFindSpec spec = specs.get(specIndex);
			String replacement = MatchResult.expandToTemplate(spec.replacement(), captures);
			if (replacedTextBuilder.length() > 0) {
				replacedTextBuilder.append(" ... ");
				fromPatternBuilder.append(" / ");
				toPatternBuilder.append(" / ");
			}
			replacedTextBuilder.append(replacement);
			fromPatternBuilder
					.append(spec.pattern().stream().map(ConversionToken::getValue).collect(Collectors.joining(" ")));
			toPatternBuilder.append(spec.replacement());
		}

		for (int[] si : stepIndices) {
			int start = si[0];
			int end = si[1];
			int specIndex = si[2];
			MRuleFindSpec spec = specs.get(specIndex);
			String replacement = MatchResult.expandToTemplate(spec.replacement(), captures);

			// Safety check: ensure indices are still valid after prior deletions
			if (start > result.size() || end > result.size()) {
				LOGGER.warn("MultiReplace: インデックス範囲外 [{}..{}) size={}, スキップ", start, end, result.size());
				continue;
			}

			AstNode firstNode = result.get(start);
			List<AstNode> before = new ArrayList<>(result.subList(0, start));
			List<AstNode> after = new ArrayList<>(result.subList(end, result.size()));

			before.addAll(after);
			result = before;

			if (!replacement.isEmpty()) {
				result.add(start, AstNode.tokenNodeWithId(replacement, firstNode.getLine(), firstNode.getColumn(),
						firstNode.getId(), firstNode.getStreamIndex()));
			}

			LOGGER.info("MultiReplace [{}]: [{}..{}) → [{}]", match.rule().getRuleId(), start, end, replacement);
		}

		// Record log entry for this application
		logs.add(new PhaseTransformLog(phaseName, phaseIndex, match.rule().getSourceFile(),
				fromPatternBuilder.toString(), toPatternBuilder.toString(), matchedTextBuilder.toString(),
				replacedTextBuilder.toString()));

		return result;
	}

}
