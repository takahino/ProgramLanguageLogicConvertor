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

package io.github.takahino.cpp2csharp.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * パターン発見結果を HTML (.html) に出力するライター。
 */
public class PatternDiscoveryOutputWriter {

	private static final Logger LOG = LoggerFactory.getLogger(PatternDiscoveryOutputWriter.class);

	private static final String[] COLUMNS = {"#", "パターン種別", "識別子名", "アクセス演算子", "引数数", "出現回数", "ルール有無", "ルールファイル",
			"出現ファイル"};

	/**
	 * パターン発見結果を HTML に出力する。
	 *
	 * @param outputDir
	 *            出力先ディレクトリ
	 * @param result
	 *            パターン発見結果
	 * @throws IOException
	 *             ファイル書き込みに失敗した場合
	 */
	public void write(Path outputDir, PatternDiscoveryResult result) throws IOException {
		writeHtml(outputDir, result);
	}

	// -------------------------------------------------------------------------
	// HTML 出力
	// -------------------------------------------------------------------------

	private void writeHtml(Path outputDir, PatternDiscoveryResult result) throws IOException {
		Path htmlPath = outputDir.resolve("pattern_discovery.html");
		String html = buildHtml(result);
		Files.writeString(htmlPath, html, StandardCharsets.UTF_8);
		LOG.info("HTML 出力: {}", htmlPath);
	}

	private String buildHtml(PatternDiscoveryResult result) {
		int total = result.allPatterns().size();
		int covered = result.coveredPatterns().size();
		int uncovered = result.uncoveredPatterns().size();
		double coverRate = total == 0 ? 0.0 : covered * 100.0 / total;

		StringBuilder sb = new StringBuilder();
		sb.append("""
				<!DOCTYPE html>
				<html lang="ja">
				<head>
				<meta charset="UTF-8">
				<title>パターン発見レポート</title>
				<style>
				  body { background:#1e1e1e; color:#d4d4d4; font-family:monospace; margin:20px; }
				  h1 { color:#4ec9b0; }
				  h2 { color:#9cdcfe; border-bottom:1px solid #444; padding-bottom:4px; }
				  table { border-collapse:collapse; width:100%; margin-bottom:24px; font-size:0.85em; }
				  th { background:#2d2d2d; color:#9cdcfe; padding:6px 8px; text-align:left; border:1px solid #444; }
				  td { padding:5px 8px; border:1px solid #333; vertical-align:top; }
				  tr.uncovered td { background:#3a1a1a; color:#f48771; }
				  tr.covered   td { background:#1a3a2a; color:#4ec9b0; }
				  .stat-table td { width:25%; font-size:1.1em; text-align:center; }
				  .stat-table .num { font-size:2em; font-weight:bold; color:#ce9178; }
				</style>
				</head>
				<body>
				""");

		sb.append("<h1>パターン発見レポート</h1>\n");

		// サマリー統計
		sb.append("<h2>サマリー</h2>\n");
		sb.append("<table class='stat-table'><tr>\n");
		sb.append(String.format("<td>総パターン数<br><span class='num'>%d</span></td>\n", total));
		sb.append(String.format("<td>スキャンファイル数<br><span class='num'>%d</span></td>\n", result.totalFiles()));
		sb.append(String.format("<td>カバー済み<br><span class='num'>%d</span></td>\n", covered));
		sb.append(String.format("<td>未カバー<br><span class='num'>%d</span></td>\n", uncovered));
		sb.append(String.format("<td>カバー率<br><span class='num'>%.1f%%</span></td>\n", coverRate));
		sb.append("</tr></table>\n");

		// ルール未作成テーブル
		sb.append("<h2>ルール未作成（優先度順）</h2>\n");
		appendPatternTable(sb, result.uncoveredPatterns(), "uncovered");

		// ルール作成済みテーブル
		sb.append("<h2>ルール作成済み</h2>\n");
		appendPatternTable(sb, result.coveredPatterns(), "covered");

		sb.append("</body></html>\n");
		return sb.toString();
	}

	private void appendPatternTable(StringBuilder sb, List<CandidatePattern> patterns, String cssClass) {
		if (patterns.isEmpty()) {
			sb.append("<p>（なし）</p>\n");
			return;
		}
		sb.append("<table>\n<tr>");
		for (String col : COLUMNS) {
			sb.append("<th>").append(escapeHtml(col)).append("</th>");
		}
		sb.append("</tr>\n");

		for (int i = 0; i < patterns.size(); i++) {
			CandidatePattern p = patterns.get(i);
			sb.append("<tr class='").append(cssClass).append("'>");
			sb.append("<td>").append(i + 1).append("</td>");
			sb.append("<td>").append(escapeHtml(p.type().name())).append("</td>");
			sb.append("<td>").append(escapeHtml(p.identifierName())).append("</td>");
			sb.append("<td>").append(escapeHtml(p.accessOperator() != null ? p.accessOperator() : "")).append("</td>");
			sb.append("<td>").append(p.argCount() < 0 ? "-" : p.argCount()).append("</td>");
			sb.append("<td>").append(p.occurrenceCount()).append("</td>");
			sb.append("<td>").append(p.hasRule() ? "○" : "×").append("</td>");
			sb.append("<td>").append(escapeHtml(p.ruleFile())).append("</td>");
			sb.append("<td>").append(escapeHtml(String.join(", ", p.occurrenceFiles()))).append("</td>");
			sb.append("</tr>\n");
		}
		sb.append("</table>\n");
	}

	private static String escapeHtml(String s) {
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
