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

import io.github.takahino.cpp2csharp.rule.RuleLoaderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * {@code .crule} ファイルを読み込み、{@link CombyRule} のリストを生成するクラス。
 *
 * <h2>ファイル形式</h2>
 *
 * <pre>
 * # コメント行
 * from: :[recv].Left(:[n])
 * to: :[recv].Substring(0, :[n])
 * test: str.Left(5)
 * assrt: str.Substring(0, 5)
 * </pre>
 */
public class CombyRuleLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(CombyRuleLoader.class);
	private static final Pattern PHASE_DIR_PATTERN = RuleLoaderConstants.PHASE_DIR_PATTERN;
	private static final String COMBY_RULES_PATH = "rules/comby";
	private static final String CRULE_EXTENSION = ".crule";

	/**
	 * クラスパス上の {@code rules/comby/} 配下から全フェーズのルールを読み込む。
	 * ディレクトリが存在しない場合は空のリストを返す（後方互換）。
	 */
	public List<List<CombyRule>> loadFromClasspath() throws IOException {
		URL url = getClass().getClassLoader().getResource(COMBY_RULES_PATH);
		if (url == null) {
			LOGGER.debug("rules/comby/ ディレクトリが存在しないため COMBY フェーズをスキップ");
			return List.of();
		}
		try {
			URI uri = url.toURI();
			if ("jar".equals(uri.getScheme())) {
				try (FileSystem fs = FileSystems.newFileSystem(uri, Map.of())) {
					return loadPhasesFrom(fs.getPath(COMBY_RULES_PATH));
				}
			} else {
				return loadPhasesFrom(Path.of(uri));
			}
		} catch (URISyntaxException e) {
			throw new IOException("rules/comby/ URI の解析に失敗: " + e.getMessage(), e);
		}
	}

	/** 指定ディレクトリ配下の [NN]_* サブディレクトリからフェーズ別に読み込む */
	public List<List<CombyRule>> loadPhasesFrom(Path baseDir) throws IOException {
		if (!Files.isDirectory(baseDir))
			return List.of();
		List<List<CombyRule>> phases = new ArrayList<>();
		try (Stream<Path> entries = Files.list(baseDir)) {
			List<Path> sortedDirs = entries.filter(Files::isDirectory)
					.filter(p -> PHASE_DIR_PATTERN.matcher(p.getFileName().toString()).matches())
					.sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
			for (Path phaseDir : sortedDirs) {
				List<CombyRule> phaseRules = new ArrayList<>();
				try (Stream<Path> files = Files.walk(phaseDir)) {
					files.filter(p -> p.toString().endsWith(CRULE_EXTENSION)).sorted().forEach(f -> {
						try {
							phaseRules.addAll(loadFromFile(f));
						} catch (IOException e) {
							LOGGER.warn("crule 読み込み失敗: {}", f, e);
						}
					});
				}
				if (!phaseRules.isEmpty())
					phases.add(phaseRules);
			}
		}
		return phases;
	}

	/** 単一の .crule ファイルを読み込む */
	public List<CombyRule> loadFromFile(Path path) throws IOException {
		String content = Files.readString(path, StandardCharsets.UTF_8);
		return parseContent(content, path.getFileName().toString());
	}

	/** .crule テキストをパースして CombyRule リストを返す */
	public List<CombyRule> parseContent(String content, String sourceFile) {
		List<CombyRule> rules = new ArrayList<>();
		String currentFrom = null;
		String currentTo = null;
		List<String> testInputs = new ArrayList<>();
		List<String> assrtOutputs = new ArrayList<>();

		for (String rawLine : content.split("\r?\n")) {
			String line = rawLine;
			// コメント除去
			int commentIdx = line.indexOf('#');
			if (commentIdx >= 0)
				line = line.substring(0, commentIdx);
			line = line.stripTrailing();
			if (line.isBlank())
				continue;

			if (line.startsWith("from:")) {
				// 前のルールを確定
				if (currentFrom != null && currentTo != null) {
					rules.add(buildRule(sourceFile, currentFrom, currentTo, testInputs, assrtOutputs));
				}
				currentFrom = line.substring("from:".length()).strip();
				currentTo = null;
				testInputs = new ArrayList<>();
				assrtOutputs = new ArrayList<>();
			} else if (line.startsWith("to:")) {
				currentTo = line.substring("to:".length()).strip();
			} else if (line.startsWith("test:")) {
				testInputs.add(line.substring("test:".length()).strip());
			} else if (line.startsWith("assrt:")) {
				assrtOutputs.add(line.substring("assrt:".length()).strip());
			}
		}
		// 最後のルールを確定
		if (currentFrom != null && currentTo != null) {
			rules.add(buildRule(sourceFile, currentFrom, currentTo, testInputs, assrtOutputs));
		}
		LOGGER.debug("{}: {} ルール読み込み", sourceFile, rules.size());
		return rules;
	}

	private CombyRule buildRule(String sourceFile, String from, String to, List<String> tests, List<String> assrts) {
		List<CombyTestCase> testCases = new ArrayList<>();
		int count = Math.min(tests.size(), assrts.size());
		for (int i = 0; i < count; i++) {
			testCases.add(new CombyTestCase(tests.get(i), assrts.get(i)));
		}
		return new CombyRule(sourceFile, from, to, testCases);
	}
}
