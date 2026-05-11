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

package io.github.takahino.cpp2csharp;

import io.github.takahino.cpp2csharp.converter.ConversionResult;
import io.github.takahino.cpp2csharp.converter.CppToCSharpConverter;
import io.github.takahino.cpp2csharp.discovery.PatternDiscoveryEngine;
import io.github.takahino.cpp2csharp.discovery.PatternDiscoveryOutputWriter;
import io.github.takahino.cpp2csharp.discovery.PatternDiscoveryResult;
import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.output.ConversionOutputWriter;
import io.github.takahino.cpp2csharp.rule.ConversionRule;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader.ThreePassRuleSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * VC++6 MFC → C# 変換ツールのエントリポイント。
 *
 * <h2>使用方法</h2>
 *
 * <pre>
 * java -jar cpp-to-csharp.jar &lt;入力C++ファイル&gt; [ルールディレクトリ]
 * </pre>
 *
 * <ul>
 * <li>入力ファイル: 変換対象の C++ ソースファイルパス</li>
 * <li>ルールディレクトリ (省略可): ルールファイルのディレクトリ。省略時はクラスパス上の rules/ を使用</li>
 * </ul>
 */
public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	/**
	 * エントリポイント。
	 *
	 * @param args
	 *            コマンドライン引数
	 */
	public static void main(String[] args) throws IOException {
		long startMs = System.currentTimeMillis();
		LOG.info("=== cpp2csharp 起動 ===");

		if (args.length < 1) {
			LOG.error("使用方法: cpp-to-csharp <入力C++ファイル> [ルールディレクトリ]");
			System.exit(1);
		}

		List<String> argList = new ArrayList<>(List.of(args));
		boolean discoverMode = argList.remove("--discover");

		if (argList.isEmpty()) {
			LOG.error("使用方法: cpp-to-csharp <入力C++ファイル> [ルールディレクトリ]");
			System.exit(1);
		}

		Path inputFile = Path.of(argList.get(0));
		if (!Files.exists(inputFile)) {
			LOG.error("ファイル/ディレクトリが見つかりません: {}", inputFile);
			System.exit(1);
		}

		LOG.info("ルールロード開始 (+{}ms)", System.currentTimeMillis() - startMs);
		ConversionRuleLoader loader = new ConversionRuleLoader(CppParserFactory.asLexerFactory());
		ThreePassRuleSet ruleSet;
		if (argList.size() >= 2) {
			Path rulesDir = Path.of(argList.get(1));
			if (!Files.isDirectory(rulesDir)) {
				LOG.error("ルールディレクトリが存在しません: {}", rulesDir);
				System.exit(1);
			}
			ruleSet = loader.loadThreePassRulesFrom(rulesDir);
		} else {
			ruleSet = loader.loadThreePassRules();
		}
		int totalRules = ruleSet.mainPhaseSpecs().stream().mapToInt(s -> s.rules().size()).sum();
		if (totalRules == 0) {
			System.err.println("ルールが見つかりません。引数にルールディレクトリを指定してください。");
			System.err.println("  java -jar cpp2csharp.jar <ファイル or ディレクトリ> <ルールディレクトリ>");
		}
		LOG.info("ルールロード完了 (+{}ms): main={} フェーズ, pre={} フェーズ, post={} フェーズ, comby={} フェーズ, dynamic={} スペック",
				System.currentTimeMillis() - startMs, ruleSet.mainPhaseSpecs().size(), ruleSet.prePhases().size(),
				ruleSet.postPhases().size(), ruleSet.combyPhases().size(), ruleSet.dynamicSpecs().size());

		if (discoverMode) {
			if (!Files.isDirectory(inputFile)) {
				LOG.error("--discover モードはディレクトリを指定してください: {}", inputFile);
				System.exit(1);
			}
			LOG.info("パターン発見モード: ディレクトリ={}", inputFile.toAbsolutePath().normalize());
			PatternDiscoveryEngine engine = new PatternDiscoveryEngine();
			PatternDiscoveryResult discoverResult = engine.discover(inputFile, ruleSet);
			PatternDiscoveryOutputWriter discoverWriter = new PatternDiscoveryOutputWriter();
			discoverWriter.write(inputFile, discoverResult);
			LOG.info("パターン発見完了: パターン数={}, ファイル数={}", discoverResult.allPatterns().size(), discoverResult.totalFiles());
			return;
		}

		ConversionOutputWriter writer = new ConversionOutputWriter();
		// レポート出力用にメインルールをフラット化
		List<ConversionRule> allMainRules = ruleSet.mainPhaseSpecs().stream().flatMap(s -> s.rules().stream()).toList();

		if (Files.isDirectory(inputFile)) {
			// D1: ディレクトリバッチ変換
			convertDirectory(inputFile, ruleSet, allMainRules, writer);
		} else {
			// 単一ファイル変換
			// マルチスレッド化時: ファイルごとに new すること。同一 converter の共有は Transformer の errors
			// 等が混在するため禁止。
			CppToCSharpConverter converter = new CppToCSharpConverter();

			String cppSource = Files.readString(inputFile, StandardCharsets.UTF_8);
			LOG.info("処理開始 (+{}ms): ファイル={} ({}行, {}bytes)", System.currentTimeMillis() - startMs,
					inputFile.toAbsolutePath().normalize(), cppSource.lines().count(), cppSource.length());
			LOG.info("---");

			ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);

			if (!result.getParseErrors().isEmpty()) {
				LOG.error("=== パースエラー ===");
				result.getParseErrors().forEach(LOG::error);
			}

			if (!result.getTransformErrors().isEmpty()) {
				LOG.error("=== 変換エラー ===");
				result.getTransformErrors().forEach(e -> LOG.error(e.getMessage()));
			}

			// 変換結果・レポートを入力と同じパスに出力（拡張子 .cpp / .i → .cs）
			Path outputCs = inputFile
					.resolveSibling(inputFile.getFileName().toString().replaceFirst("\\.(cpp|i)$", ".cs"));
			writer.write(inputFile, outputCs, cppSource, result, allMainRules); // A2: main rules を渡す
			LOG.info("出力: {}, .report.txt, .report.html, .treedump.txt", outputCs);

			LOG.info("---");
			LOG.info("処理完了: ファイル={} (パースエラー: {}, 変換エラー: {})", inputFile.toAbsolutePath().normalize(),
					result.getParseErrors().size(), result.getTransformErrors().size());
		}
	}

	/**
	 * D1 ディレクトリバッチ変換: ディレクトリ内の全 C++ ファイルをまとめて変換する。
	 *
	 * <p>
	 * <strong>なぜ追加したか</strong>: 実運用は複数ファイルが前提であり、
	 * ファイルごとに手動で実行するのは非現実的なため。ルールセットを一度だけ読み込み（読み取り専用で共有可能）、 各ファイルを parallelStream
	 * で並列変換することで大規模コードベースにも対応できる。 {@code Transformer} はスレッドセーフでないため、ファイルごとに
	 * {@code new CppToCSharpConverter()} を生成する（スレッド安全の制約を維持）。
	 * </p>
	 *
	 * @param inputDir
	 *            変換対象ディレクトリ
	 * @param rules
	 *            変換ルールリスト（読み取り専用、スレッド間共有可能）
	 * @param writer
	 *            出力ライター
	 * @throws IOException
	 *             ファイル走査に失敗した場合
	 */
	private static void convertDirectory(Path inputDir, ThreePassRuleSet ruleSet, List<ConversionRule> allMainRules,
			ConversionOutputWriter writer) throws IOException {
		List<Path> cppFiles;
		try (var stream = Files.walk(inputDir)) {
			cppFiles = stream.filter(Files::isRegularFile).filter(p -> p.toString().matches(".*\\.(cpp|c|h|i)$"))
					.sorted().toList();
		}

		LOG.info("バッチ変換開始: ディレクトリ={}, 対象ファイル数={}", inputDir.toAbsolutePath().normalize(), cppFiles.size());
		if (cppFiles.isEmpty()) {
			LOG.warn("変換対象ファイルが見つかりません (.cpp/.c/.h/.i)");
			return;
		}

		AtomicInteger successCount = new AtomicInteger();
		AtomicInteger errorCount = new AtomicInteger();

		record FileResult(Path path, ConversionResult result) {
		}

		// ファイルごとに new CppToCSharpConverter() — Transformer はスレッドセーフでないため必須
		List<FileResult> fileResults = cppFiles.parallelStream().map(cppFile -> {
			try {
				CppToCSharpConverter converter = new CppToCSharpConverter();
				String cppSource = Files.readString(cppFile, StandardCharsets.UTF_8);
				ConversionResult result = converter.convertSourceThreePass(cppSource, ruleSet);
				Path outputCs = cppFile
						.resolveSibling(cppFile.getFileName().toString().replaceFirst("\\.(cpp|c|h|i)$", ".cs"));
				writer.write(cppFile, outputCs, cppSource, result, allMainRules); // A2: main rules を渡す
				LOG.info("変換完了: {} → {}", cppFile.getFileName(), outputCs.getFileName());
				successCount.incrementAndGet();
				return new FileResult(cppFile, result);
			} catch (IOException e) {
				LOG.error("変換失敗: {} — {}", cppFile, e.getMessage());
				errorCount.incrementAndGet();
				return null;
			}
		}).filter(f -> f != null).toList();

		List<Path> processedFiles = fileResults.stream().map(FileResult::path).toList();
		List<ConversionResult> allConversionResults = fileResults.stream().map(FileResult::result).toList();

		// プロジェクト全体サマリーを出力
		writeProjectSummary(inputDir, cppFiles, successCount.get(), errorCount.get(), allConversionResults,
				allMainRules);
		LOG.info("---");
		LOG.info("バッチ変換完了: 成功={}, 失敗={} / 合計={}", successCount.get(), errorCount.get(), cppFiles.size());
	}

	/**
	 * プロジェクト全体のバッチ変換サマリーを入力ディレクトリに出力する。
	 *
	 * @param inputDir
	 *            変換対象ディレクトリ
	 * @param cppFiles
	 *            変換対象ファイルリスト
	 * @param successCount
	 *            変換成功数
	 * @param errorCount
	 *            変換失敗数
	 * @param allResults
	 *            全ファイルの変換結果（ルール有効度統計用）
	 * @param allMainRules
	 *            全変換ルール（ルール有効度統計用）
	 * @throws IOException
	 *             ファイル書き込みに失敗した場合
	 */
	private static void writeProjectSummary(Path inputDir, List<Path> cppFiles, int successCount, int errorCount,
			List<ConversionResult> allResults, List<ConversionRule> allMainRules) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("========================================\n");
		sb.append("  バッチ変換サマリー\n");
		sb.append("========================================\n\n");
		sb.append("対象ディレクトリ: ").append(inputDir.toAbsolutePath().normalize()).append("\n");
		sb.append("総ファイル数    : ").append(cppFiles.size()).append(" 件\n");
		sb.append("変換成功        : ").append(successCount).append(" 件\n");
		sb.append("変換失敗        : ").append(errorCount).append(" 件\n\n");
		sb.append("--- 変換対象ファイル一覧 ---\n");
		for (Path f : cppFiles) {
			sb.append("  ").append(inputDir.relativize(f)).append("\n");
		}
		sb.append("\n");
		sb.append(ConversionOutputWriter.buildBatchRuleEffectivenessSection(allResults, allMainRules));
		Path summaryPath = inputDir.resolve("batch_conversion_summary.txt");
		Files.writeString(summaryPath, sb.toString(), StandardCharsets.UTF_8);
		LOG.info("バッチサマリー出力: {}", summaryPath);
	}

}
