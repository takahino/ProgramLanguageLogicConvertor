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

package io.github.takahino.cpp2csharp.transform.strategy;

import io.github.takahino.cpp2csharp.matcher.MatchResult;
import io.github.takahino.cpp2csharp.rule.ConversionRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 右端優先・最短範囲優先・特異性優先でマッチを選択する戦略。
 *
 * <p>
 * AST 非依存の純粋トークン位置ベースの選択アルゴリズム。
 * </p>
 *
 * <ul>
 * <li>Step 0: 引数個数フィルタ + 先頭 ABSTRACT_PARAM フィルタ + RECEIVER デデュープ</li>
 * <li>Step 1: startIndex 最大（右端＝内側優先）</li>
 * <li>Step 2: endIndex 最小（最短範囲優先）</li>
 * <li>Step 3: 具体トークン数最大（特異性優先）</li>
 * </ul>
 */
public class RightmostFirstSelectionStrategy implements MatchSelectionStrategy {

	private static final Set<String> OPEN_BRACKETS = Set.of("(", "[", "{");
	private static final Set<String> CLOSE_BRACKETS = Set.of(")", "]", "}");

	private static final String STRATEGY_NAME = "RightmostFirstSelectionStrategy";

	@Override
	public SelectionDecision selectBest(MatchSelectionInput input) {
		List<MatchResult> allMatches = input.allMatches();
		List<String> tokens = input.tokens();
		MatchSelectionContext context = input.context();

		// Step 0: 引数個数フィルタ（汎用ルールの誤適用防止）
		List<MatchResult> argFiltered = allMatches.stream().filter(m -> passesArgumentCountFilter(m, tokens))
				.filter(m -> passesLeadingAbstractParamFilter(m)).collect(Collectors.toCollection(ArrayList::new));
		if (argFiltered.isEmpty()) {
			return SelectionDecision.none(STRATEGY_NAME, "arg_filter_empty");
		}

		// Step 0b: RECEIVER先頭マッチは同一（ルール, endIndex）内で最小 startIndex のみ残す
		argFiltered = deduplicateReceiverMatches(argFiltered);
		if (argFiltered.isEmpty()) {
			return SelectionDecision.none(STRATEGY_NAME, "receiver_dedup_empty");
		}

		List<MatchResult> rightmostMatches = filterByMaxInt(argFiltered, MatchResult::getStartIndex);
		List<MatchResult> candidateMatches = filterByMinInt(rightmostMatches, MatchResult::getEndIndex);
		List<MatchResult> bestMatches = filterByMaxInt(candidateMatches, m -> countConcreteTokens(m.getRule()));

		if (bestMatches.size() > 1) {
			int maxStart = bestMatches.get(0).getStartIndex();
			int minEnd = bestMatches.get(0).getEndIndex();
			String contextTokens = String.join(" ", tokens.subList(maxStart, minEnd));
			if (context != null) {
				context.reportAmbiguous(bestMatches, contextTokens);
			}
			return SelectionDecision.none(STRATEGY_NAME, "ambiguous");
		}

		MatchResult best = bestMatches.get(0);
		String reason = String.format("startIndex=%d,endIndex=%d,specificity=%d", best.getStartIndex(),
				best.getEndIndex(), countConcreteTokens(best.getRule()));
		return SelectionDecision.selected(best, STRATEGY_NAME, reason);
	}

	/**
	 * RECEIVER が先頭に来るマッチを（ルール, endIndex）単位でデデュープし、 同一キー内で startIndex
	 * が最小のもの（最長レシーバー）のみを残す。
	 */
	private List<MatchResult> deduplicateReceiverMatches(List<MatchResult> matches) {
		record RuleEndKey(ConversionRule rule, int endIndex) {
		}

		List<MatchResult> result = new ArrayList<>();
		Map<RuleEndKey, MatchResult> receiverBest = new HashMap<>();

		for (MatchResult m : matches) {
			var fromTokens = m.getRule().getFromTokens();
			if (!fromTokens.isEmpty() && fromTokens.get(0).isReceiverParam()) {
				var key = new RuleEndKey(m.getRule(), m.getEndIndex());
				MatchResult existing = receiverBest.get(key);
				if (existing == null || m.getStartIndex() < existing.getStartIndex()) {
					receiverBest.put(key, m);
				}
			} else {
				result.add(m);
			}
		}
		result.addAll(receiverBest.values());
		return result;
	}

	/**
	 * from パターンの先頭トークンが ABSTRACT_PARAM・ABSTRACT_TOKEN の場合、そのキャプチャ内容が
	 * 単純な識別子・リテラル等であることを確認し、不適切なマッチを除外する。
	 *
	 * <p>
	 * 以下のいずれかに該当するキャプチャは拒否する:
	 * </p>
	 * <ul>
	 * <li>(a) 括弧で始まる（演算子優先度の誤解析防止）</li>
	 * <li>(b) 深さ0で {@code .} を含み、かつ深さ0で {@code (}
	 * を含む（ドットチェーン内の関数呼び出しが別ルールで処理されるべき場合）<br>
	 * {@code this . m_str} のような単純メンバアクセスは許可する</li>
	 * <li>(c) {@code identifier ( ... )} 形式（レシーバー付き呼び出しは RECEIVER ルールで処理）</li>
	 * </ul>
	 *
	 * <p>
	 * ABSTRACT_TOKEN は1トークンキャプチャのため (b)(c) には該当しないが、(a) の括弧記号単体のキャプチャは
	 * 拒否する。キャプチャ取得は {@code getCaptureKey()} 経由で行い、ABSTRACT_PARAM (0-99) と
	 * ABSTRACT_TOKEN (200-299) のキー空間を正しく扱う。
	 * </p>
	 *
	 * <p>
	 * LR寄せ計画: 事後フィルタ頼みを減らし、将来的には候補生成側で誤候補を減らす方針。
	 * </p>
	 */
	private boolean passesLeadingAbstractParamFilter(MatchResult match) {
		var fromTokens = match.getRule().getFromTokens();
		if (fromTokens.isEmpty())
			return true;
		var first = fromTokens.get(0);
		if (!first.isAbstractParam() && !first.isAbstractTokenParam())
			return true;
		var captured = match.getCapturedTokens(first.getCaptureKey());
		return !captured.isEmpty() && !startsWithBracket(captured) && !hasDotWithParenAtDepthZero(captured)
				&& !isFunctionCallForm(captured);
	}

	/** キャプチャトークン列が開き括弧または閉じ括弧で始まるかを判定する。 */
	private static boolean startsWithBracket(List<String> tokens) {
		String first = tokens.get(0);
		return OPEN_BRACKETS.contains(first) || CLOSE_BRACKETS.contains(first);
	}

	/**
	 * 括弧深度0で {@code .} と {@code (} の両方を含むかを判定する。 {@code this . m_str}
	 * のような単純メンバアクセスは false（許可）。 {@code time . Format ( "%Y/%m/%d" )}
	 * のような関数呼び出しを含むドットチェーンは true（拒否）。
	 *
	 * <p>
	 * <b>実装意図:</b> 本メソッドは {@code passesLeadingAbstractParamFilter} の (b) で使用され、
	 * 先頭トークンが {@code ABSTRACT_PARAM00} のルールにのみ適用される。現行ルールセットには 先頭が
	 * {@code ABSTRACT_PARAM00} のルールは存在せず（先頭は識別子・{@code RECEIVER}・ デコレータ {@code ::}
	 * 等）、本分岐は将来のルール追加や防御的実装として残している。
	 * </p>
	 */
	private static boolean hasDotWithParenAtDepthZero(List<String> tokens) {
		int depth = 0;
		boolean hasDotAtDepthZero = false;
		boolean hasParenAtDepthZero = false;
		for (String t : tokens) {
			if (OPEN_BRACKETS.contains(t)) {
				if (depth == 0)
					hasParenAtDepthZero = true;
				depth++;
			} else if (CLOSE_BRACKETS.contains(t)) {
				if (depth > 0)
					depth--;
			} else if (depth == 0) {
				if (".".equals(t))
					hasDotAtDepthZero = true;
			}
			if (hasDotAtDepthZero && hasParenAtDepthZero)
				return true;
		}
		return false;
	}

	/**
	 * トークン列が {@code identifier ( ... )} 形式かを判定する。 この形式はレシーバー付き呼び出しを示し、RECEIVER
	 * ルールで処理されるべきものを除外するために使う。
	 */
	private static boolean isFunctionCallForm(List<String> tokens) {
		return tokens.size() >= 3 && "(".equals(tokens.get(1)) && ")".equals(tokens.get(tokens.size() - 1));
	}

	/**
	 * ルールが引数個数制約を持つ場合、実際のトークン列の引数個数と照合してフィルタリングする。
	 * 引数個数制約がないルール（{@code argumentCount < 0}）は常に通過させる。
	 */
	private boolean passesArgumentCountFilter(MatchResult match, List<String> tokens) {
		int expected = match.getRule().getArgumentCount();
		if (expected < 0) {
			return true;
		}
		int actual = computeActualArgumentCount(tokens, match.getStartIndex(), match.getEndIndex());
		return actual >= 0 && actual == expected;
	}

	/**
	 * トークン列の {@code [start, end)} 範囲内の最初の {@code (} と最後の {@code )} を探し、
	 * その間の深さ0のカンマ数から引数個数を計算する。 括弧が見つからない場合や不正な範囲の場合は {@code -1} を返す。
	 */
	private int computeActualArgumentCount(List<String> tokens, int start, int end) {
		List<String> slice = tokens.subList(start, end);
		int openIdx = firstIndexOf(slice, "(");
		int closeIdx = lastIndexOf(slice, ")");
		if (openIdx < 0 || closeIdx < 0 || openIdx >= closeIdx) {
			return -1;
		}
		if (openIdx + 1 == closeIdx) {
			return 0;
		}
		int commaCount = 0;
		int depth = 0;
		for (int i = openIdx + 1; i < closeIdx; i++) {
			String t = slice.get(i);
			if (OPEN_BRACKETS.contains(t)) {
				depth++;
			} else if (CLOSE_BRACKETS.contains(t)) {
				if (depth > 0)
					depth--;
			} else if (depth == 0 && ",".equals(t)) {
				commaCount++;
			}
		}
		return commaCount + 1;
	}

	/** {@code key} の値が最大のものだけを残したリストを返す。 */
	private static List<MatchResult> filterByMaxInt(List<MatchResult> items, ToIntFunction<MatchResult> key) {
		int max = items.stream().mapToInt(key).max().orElseThrow();
		return items.stream().filter(m -> key.applyAsInt(m) == max).toList();
	}

	/** {@code key} の値が最小のものだけを残したリストを返す。 */
	private static List<MatchResult> filterByMinInt(List<MatchResult> items, ToIntFunction<MatchResult> key) {
		int min = items.stream().mapToInt(key).min().orElseThrow();
		return items.stream().filter(m -> key.applyAsInt(m) == min).toList();
	}

	/** リスト先頭から {@code target} を探し、最初に一致した位置を返す。見つからない場合は {@code -1}。 */
	private static int firstIndexOf(List<String> tokens, String target) {
		for (int i = 0; i < tokens.size(); i++) {
			if (target.equals(tokens.get(i)))
				return i;
		}
		return -1;
	}

	/** リスト末尾から {@code target} を探し、最後に一致した位置を返す。見つからない場合は {@code -1}。 */
	private static int lastIndexOf(List<String> tokens, String target) {
		for (int i = tokens.size() - 1; i >= 0; i--) {
			if (target.equals(tokens.get(i)))
				return i;
		}
		return -1;
	}
}
