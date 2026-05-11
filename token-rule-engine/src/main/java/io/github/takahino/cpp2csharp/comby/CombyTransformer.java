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

package io.github.takahino.cpp2csharp.comby;

import io.github.takahino.comby.Comby;
import io.github.takahino.comby.core.model.CapturedValue;
import io.github.takahino.comby.core.model.Match;
import io.github.takahino.comby.core.model.MatchEnvironment;
import io.github.takahino.cpp2csharp.converter.PhaseTransformLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * COMBYルール群をソーステキストに反復適用するトランスフォーマー。
 *
 * <p>
 * 各フェーズ内で全ルールの全マッチを収集し、最も右端（start 位置が最大）の
 * マッチを1件適用する。変換が収束（マッチなし）するまで最大100回繰り返す。
 * </p>
 *
 * <p>
 * structural-rewriter ライブラリの {@code Comby.matches()} API を使用した実装。
 * </p>
 */
public class CombyTransformer implements CombyEngine {

	private static final Logger LOGGER = LoggerFactory.getLogger(CombyTransformer.class);
	private static final int MAX_ITERATIONS = 100;
	private static final String LANGUAGE = "generic";
	private static final Pattern TO_HOLE_PATTERN = Pattern.compile(":\\[([a-zA-Z_][a-zA-Z0-9_]*)\\]");

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
	 * 複数フェーズを順に適用する。各フェーズは収束まで反復適用される。
	 */
	public String transformPhases(String text, List<List<CombyRule>> phases) {
		String current = text;
		int phaseIndex = 1;
		for (List<CombyRule> phase : phases) {
			current = transformPhase(current, phase, phaseIndex++);
		}
		return current;
	}

	/**
	 * 1フェーズ分のルール群を収束まで適用する（後方互換オーバーロード）。
	 */
	public String transformPhase(String text, List<CombyRule> rules) {
		return transformPhase(text, rules, 1);
	}

	/**
	 * 1フェーズ分のルール群を収束まで適用する。
	 *
	 * @param text
	 *            変換対象テキスト
	 * @param rules
	 *            適用するルールリスト
	 * @param phaseIndex
	 *            フェーズ番号（1始まり）
	 * @return 変換後テキスト
	 */
	public String transformPhase(String text, List<CombyRule> rules, int phaseIndex) {
		String current = text;
		for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
			RuleMatch best = findRightmostMatch(current, rules);
			if (best == null)
				break;
			Match m = best.match();
			String matchedText = m.matchedText();
			String expanded = expandTemplate(best.rule().getToTemplate(), m.environment());
			current = current.substring(0, m.range().start().offset()) + expanded
					+ current.substring(m.range().end().offset());
			LOGGER.debug("COMBY 適用: [{}..{}] → '{}'", m.range().start().offset(), m.range().end().offset(), expanded);
			logs.add(new PhaseTransformLog("COMBY", phaseIndex, best.rule().getSourceFile(),
					best.rule().getFromPattern(), best.rule().getToTemplate(), matchedText, expanded));
		}
		return current;
	}

	/**
	 * 全ルール・全マッチの中から最右端マッチとその適用ルールを返す。
	 *
	 * <p>
	 * リテラル開始パターン（ホール以外で始まるパターン）の場合、識別子の途中から マッチする候補をスキップする。これにより {@code List<:[t]>}
	 * → {@code IList<:[t]>} 変換後に {@code IList} 内の {@code List} が再マッチして収束しない問題を防ぐ。
	 * </p>
	 *
	 * @return 最右端 {@link RuleMatch}、マッチなしの場合は {@code null}
	 */
	private RuleMatch findRightmostMatch(String text, List<CombyRule> rules) {
		RuleMatch best = null;
		for (CombyRule rule : rules) {
			boolean literalStart = !rule.getFromPattern().startsWith(":[");
			for (Match m : Comby.matches(text, rule.getFromPattern(), LANGUAGE)) {
				int start = m.range().start().offset();
				// リテラル開始パターンで識別子途中マッチならスキップ
				if (literalStart && start > 0 && isIdentChar(text.charAt(start - 1))
						&& isIdentChar(text.charAt(start))) {
					continue;
				}
				if (best == null || start > best.match().range().start().offset()
						|| (start == best.match().range().start().offset()
								&& m.range().end().offset() < best.match().range().end().offset())) {
					best = new RuleMatch(m, rule);
				}
			}
		}
		return best;
	}

	private static boolean isIdentChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	private String expandTemplate(String toTemplate, MatchEnvironment env) {
		Matcher hm = TO_HOLE_PATTERN.matcher(toTemplate);
		StringBuffer sb = new StringBuffer();
		while (hm.find()) {
			String name = hm.group(1);
			String value = env.get(name).map(CapturedValue::value).orElse(hm.group(0));
			hm.appendReplacement(sb, Matcher.quoteReplacement(value));
		}
		hm.appendTail(sb);
		return sb.toString();
	}

	private record RuleMatch(Match match, CombyRule rule) {
	}
}
