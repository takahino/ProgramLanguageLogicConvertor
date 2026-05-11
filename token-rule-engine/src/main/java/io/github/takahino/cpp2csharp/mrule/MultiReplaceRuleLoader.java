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

package io.github.takahino.cpp2csharp.mrule;

import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionToken;
import io.github.takahino.cpp2csharp.rule.RuleLoaderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * {@code .mrule} ファイルを読み込み、{@link MultiReplaceRule} のリストを生成するクラス。
 *
 * <h2>ファイル形式</h2>
 *
 * <pre>
 * # comment
 * scope: block
 *
 * find: BOOL ( ABSTRACT_PARAM00 )
 * replace: bool(ABSTRACT_PARAM00)
 * skip:
 * find: return ABSTRACT_PARAM01
 * replace: return ABSTRACT_PARAM00
 *
 * find: BOOL
 * replace: bool
 * </pre>
 *
 * <ul>
 * <li>{@code scope:} はルールブロック全体に適用される（省略時は NONE）</li>
 * <li>{@code skip:} は次の {@code find:} の前に置き、skipBefore=true を設定する</li>
 * <li>空行はルールブロックの区切りとなる</li>
 * <li>ルール ID は {@code sourceFile:ruleIndex} 形式で生成される</li>
 * </ul>
 */
public class MultiReplaceRuleLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultiReplaceRuleLoader.class);

	private static final Pattern PHASE_DIR_PATTERN = RuleLoaderConstants.PHASE_DIR_PATTERN;

	private static final String SCOPE_PREFIX = "scope:";
	private static final String FIND_PREFIX = "find:";
	private static final String REPLACE_PREFIX = "replace:";
	private static final String SKIP_KEYWORD = "skip:";

	private final ConversionRuleLoader conversionRuleLoader;

	/**
	 * デフォルトコンストラクタ。lexerFactory が null の場合、tokenize 呼び出し時に例外がスローされる。
	 */
	public MultiReplaceRuleLoader() {
		this(null);
	}

	/**
	 * LanguageLexerFactory を注入するコンストラクタ。
	 *
	 * @param lexerFactory
	 *            言語固有の Lexer を生成するファクトリ
	 */
	public MultiReplaceRuleLoader(io.github.takahino.cpp2csharp.rule.LanguageLexerFactory lexerFactory) {
		this.conversionRuleLoader = new ConversionRuleLoader(lexerFactory);
	}

	/**
	 * 指定したベースディレクトリ配下の {@code [NN]_*} サブディレクトリから {@code .mrule} ファイルをフェーズ別に読み込む。
	 *
	 * @param baseDir
	 *            ベースディレクトリ（pre/ または post/ 等）
	 * @return フェーズごとのルールリスト（各要素はそのフェーズのルール）
	 * @throws IOException
	 *             読み込みに失敗した場合
	 */
	public List<List<MultiReplaceRule>> loadFrom(Path baseDir) throws IOException {
		List<List<MultiReplaceRule>> result = new ArrayList<>();

		if (!Files.isDirectory(baseDir)) {
			LOGGER.warn("ベースディレクトリが存在しません: {}", baseDir);
			return result;
		}

		List<Path> phaseDirs = new ArrayList<>();
		try (Stream<Path> entries = Files.list(baseDir)) {
			entries.filter(Files::isDirectory)
					.filter(p -> PHASE_DIR_PATTERN.matcher(p.getFileName().toString()).matches())
					.forEach(phaseDirs::add);
		}

		if (phaseDirs.isEmpty()) {
			// フェーズディレクトリなし → 直下の .mrule を単一フェーズとして読み込む
			List<MultiReplaceRule> flatRules = new ArrayList<>();
			try (Stream<Path> files = Files.list(baseDir)) {
				files.filter(p -> !Files.isDirectory(p) && p.toString().endsWith(".mrule")).sorted()
						.forEach(p -> flatRules.addAll(loadRulesFromFile(p)));
			}
			if (!flatRules.isEmpty()) {
				result.add(flatRules);
			}
		} else {
			phaseDirs.sort(Comparator.comparing(p -> {
				Matcher m = PHASE_DIR_PATTERN.matcher(p.getFileName().toString());
				return m.matches() ? Integer.parseInt(m.group(1)) : 999;
			}));
			for (Path phaseDir : phaseDirs) {
				List<MultiReplaceRule> phaseRules = new ArrayList<>();
				try (Stream<Path> files = Files.list(phaseDir)) {
					files.filter(p -> p.toString().endsWith(".mrule")).sorted()
							.forEach(p -> phaseRules.addAll(loadRulesFromFile(p)));
				}
				if (!phaseRules.isEmpty()) {
					result.add(phaseRules);
					LOGGER.debug("mrule フェーズ {}: {} ルール読み込み", phaseDir.getFileName(), phaseRules.size());
				}
			}
		}

		return result;
	}

	private List<MultiReplaceRule> loadRulesFromFile(Path filePath) {
		try {
			return loadFromFile(filePath);
		} catch (IOException e) {
			LOGGER.warn("mrule ファイルの読み込みに失敗: {} - {}", filePath, e.getMessage());
			return List.of();
		}
	}

	/**
	 * 単一の {@code .mrule} ファイルを読み込み、{@link MultiReplaceRule} のリストを返す。
	 *
	 * @param filePath
	 *            .mrule ファイルのパス
	 * @return 読み込んだルールのリスト
	 * @throws IOException
	 *             ファイル読み込みに失敗した場合
	 */
	public List<MultiReplaceRule> loadFromFile(Path filePath) throws IOException {
		List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		return parseLines(lines, filePath.getFileName().toString());
	}

	/**
	 * テキストコンテンツから直接ルールをパースする（テスト用）。
	 *
	 * @param content
	 *            ルール定義のテキスト
	 * @param sourceName
	 *            ソース名（デバッグ用）
	 * @return 読み込んだルールのリスト
	 */
	public List<MultiReplaceRule> loadFromString(String content, String sourceName) {
		String[] lines = content.split("\r?\n");
		return parseLines(List.of(lines), sourceName);
	}

	private List<MultiReplaceRule> parseLines(List<String> lines, String sourceName) {
		List<MultiReplaceRule> rules = new ArrayList<>();
		int ruleIndex = 0;

		// State machine: parse rule blocks separated by blank lines
		// A "block" starts when we see the first find: after the previous block ended
		MRuleScope currentScope = MRuleScope.NONE;
		List<MRuleFindSpec> currentSpecs = new ArrayList<>();
		String currentFind = null;
		String currentReplace = null;
		boolean nextSkipBefore = false;
		boolean inBlock = false;

		for (String rawLine : lines) {
			String line = rawLine.trim();

			// Skip comment lines
			if (line.startsWith("#")) {
				continue;
			}

			// Blank line: if we are in a block, flush the current spec and end the block
			if (line.isEmpty()) {
				if (inBlock) {
					flushSpec(currentSpecs, currentFind, currentReplace, nextSkipBefore, sourceName);
					currentFind = null;
					currentReplace = null;
					nextSkipBefore = false;

					if (!currentSpecs.isEmpty()) {
						String ruleId = sourceName + ":" + ruleIndex;
						rules.add(new MultiReplaceRule(ruleId, sourceName, currentScope, currentSpecs));
						ruleIndex++;
					}
					currentScope = MRuleScope.NONE;
					currentSpecs = new ArrayList<>();
					inBlock = false;
				}
				continue;
			}

			if (line.startsWith(SCOPE_PREFIX)) {
				String scopeStr = line.substring(SCOPE_PREFIX.length()).trim().toUpperCase();
				try {
					currentScope = MRuleScope.valueOf(scopeStr);
				} catch (IllegalArgumentException e) {
					LOGGER.warn("不明な scope 値: {} in {}", scopeStr, sourceName);
					currentScope = MRuleScope.NONE;
				}
				continue;
			}

			if (line.equals(SKIP_KEYWORD) || line.startsWith(SKIP_KEYWORD + " ")) {
				// skip: marks the NEXT find as skipBefore=true
				// First flush any pending spec (shouldn't happen normally, but be safe)
				flushSpec(currentSpecs, currentFind, currentReplace, nextSkipBefore, sourceName);
				currentFind = null;
				currentReplace = null;
				nextSkipBefore = true;
				continue;
			}

			if (line.startsWith(FIND_PREFIX)) {
				// Flush previous find/replace pair if any.
				// nextSkipBefore is NOT reset here: skip: propagates to all subsequent find: in
				// the block.
				if (currentFind != null) {
					flushSpec(currentSpecs, currentFind, currentReplace, nextSkipBefore, sourceName);
				}
				currentFind = line.substring(FIND_PREFIX.length()).trim();
				currentReplace = null;
				inBlock = true;
				continue;
			}

			if (line.startsWith(REPLACE_PREFIX)) {
				currentReplace = line.substring(REPLACE_PREFIX.length()).trim();
				continue;
			}

			LOGGER.warn("不明な行形式: {} : {}", sourceName, line);
		}

		// Flush last block
		if (inBlock) {
			flushSpec(currentSpecs, currentFind, currentReplace, nextSkipBefore, sourceName);
			if (!currentSpecs.isEmpty()) {
				String ruleId = sourceName + ":" + ruleIndex;
				rules.add(new MultiReplaceRule(ruleId, sourceName, currentScope, currentSpecs));
			}
		}

		LOGGER.debug("mrule 読み込み完了: {} -> {} ルール", sourceName, rules.size());
		return rules;
	}

	private void flushSpec(List<MRuleFindSpec> specs, String find, String replace, boolean skipBefore,
			String sourceName) {
		if (find == null) {
			return;
		}
		if (replace == null) {
			LOGGER.warn("find: に対応する replace: がありません: {} find={}", sourceName, find);
			return;
		}
		try {
			List<ConversionToken> pattern = conversionRuleLoader.tokenizePattern(find);
			specs.add(new MRuleFindSpec(pattern, replace, skipBefore));
		} catch (IllegalArgumentException e) {
			LOGGER.warn("find パターンのトークン化に失敗: {} pattern={} error={}", sourceName, find, e.getMessage());
		}
	}
}
