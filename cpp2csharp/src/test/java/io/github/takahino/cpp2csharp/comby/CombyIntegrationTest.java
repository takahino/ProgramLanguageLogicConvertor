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

import io.github.takahino.cpp2csharp.converter.ConversionResult;
import io.github.takahino.cpp2csharp.converter.CppToCSharpConverter;
import io.github.takahino.cpp2csharp.output.ConversionOutputWriter;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader.ThreePassRuleSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * COMBY フェーズの統合テスト。
 *
 * <p>
 * catch_cexception.crule と null_check.crule を用いて複数行構造の変換を検証し、 結果を
 * {@code outputs/test/} に出力する。
 * </p>
 */
@DisplayName("COMBY フェーズ統合テスト")
class CombyIntegrationTest {

	private static final Path OUTPUT_DIR = Paths.get(System.getProperty("user.dir")).resolve("outputs/test");

	private CppToCSharpConverter converter;
	private ConversionOutputWriter writer;
	private CombyRuleLoader combyRuleLoader;

	@BeforeEach
	void setUp() {
		converter = new CppToCSharpConverter();
		writer = new ConversionOutputWriter();
		combyRuleLoader = new CombyRuleLoader();
	}

	// =========================================================================
	// テストメソッド
	// =========================================================================

	@Test
	@DisplayName("catchCExceptionSingleLine: 単行 catch ブロックの CException → Exception 変換")
	void catchCExceptionSingleLine() throws IOException {
		String cpp = "void f() { try { doWork(); } catch ( CException * e ) { e->Delete(); } }";

		List<CombyRule> catchRules = loadCatchCExceptionRule();
		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(catchRules),
				List.of());

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSet);
		writeOutput("comby_catch_single", cpp, result);

		assertThat(result.getCsCode()).contains("catch ( Exception e )");
		assertOutputFilesExist("comby_catch_single");
	}

	@Test
	@DisplayName("catchCExceptionMultiLine: 複数行 catch ブロックの CException → Exception 変換")
	void catchCExceptionMultiLine() throws IOException {
		String cpp = "void f() {\n" + "    try {\n" + "        doWork();\n" + "        doMore();\n"
				+ "    } catch ( CException * e ) {\n" + "        e->ReportError();\n" + "        e->Delete();\n"
				+ "        return;\n" + "    }\n" + "}";

		List<CombyRule> catchRules = loadCatchCExceptionRule();
		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(catchRules),
				List.of());

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSet);
		writeOutput("comby_catch_multiline", cpp, result);

		assertThat(result.getCsCode()).contains("catch ( Exception e )");
		// ボディが保持されていること
		assertThat(result.getCsCode()).contains("e->ReportError()");
		assertThat(result.getCsCode()).contains("e->Delete()");
		assertOutputFilesExist("comby_catch_multiline");
	}

	@Test
	@DisplayName("nullCheckBlockReplacement: NULL → null の if ブロック変換")
	void nullCheckBlockReplacement() throws IOException {
		String cpp = "void f(Obj* pObj) { if ( pObj != NULL ) { pObj->Process(); } }";

		List<CombyRule> nullRules = loadNullCheckRule();
		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(nullRules), List.of());

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSet);
		writeOutput("comby_null_check", cpp, result);

		assertThat(result.getCsCode()).contains("!= null");
		assertOutputFilesExist("comby_null_check");
	}

	@Test
	@DisplayName("combyPhaseCombined: catch_cexception + null_check の同一フェーズ適用")
	void combyPhaseCombined() throws IOException {
		String cpp = "void f(Obj* pObj) {\n" + "    if ( pObj != NULL ) {\n" + "        try {\n"
				+ "            pObj->DoWork();\n" + "        } catch ( CException * e ) {\n"
				+ "            e->Delete();\n" + "        }\n" + "    }\n" + "}";

		List<CombyRule> catchRules = loadCatchCExceptionRule();
		List<CombyRule> nullRules = loadNullCheckRule();
		// 同一フェーズに両ルールを含める
		List<CombyRule> combinedPhase = new java.util.ArrayList<>();
		combinedPhase.addAll(catchRules);
		combinedPhase.addAll(nullRules);

		ThreePassRuleSet ruleSet = new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(combinedPhase),
				List.of());

		ConversionResult result = converter.convertSourceThreePass(cpp, ruleSet);
		writeOutput("comby_combined", cpp, result);

		assertThat(result.getCsCode()).contains("catch ( Exception e )");
		assertThat(result.getCsCode()).contains("!= null");
		assertOutputFilesExist("comby_combined");
	}

	// =========================================================================
	// ヘルパー
	// =========================================================================

	private List<CombyRule> loadCatchCExceptionRule() {
		String content = "# CException catch ブロック → Exception catch ブロック（複数行ボディ対応）\n"
				+ "from: catch ( CException * :[var] ) {:[body]}\n" + "to: catch ( Exception :[var] ) {:[body]}\n"
				+ "test: catch ( CException * e ) { e->Delete(); }\n"
				+ "assrt: catch ( Exception e ) { e->Delete(); }\n";
		return combyRuleLoader.parseContent(content, "catch_cexception.crule");
	}

	private List<CombyRule> loadNullCheckRule() {
		String content = "# NULL ポインタチェックブロック（複数行ボディ対応）\n" + "from: if ( :[ptr] != NULL ) {:[body]}\n"
				+ "to: if ( :[ptr] != null ) {:[body]}\n" + "test: if ( p != NULL ) { p->DoWork(); }\n"
				+ "assrt: if ( p != null ) { p->DoWork(); }\n";
		return combyRuleLoader.parseContent(content, "null_check.crule");
	}

	private void writeOutput(String basename, String cpp, ConversionResult result) throws IOException {
		Files.createDirectories(OUTPUT_DIR);
		Path inputPath = OUTPUT_DIR.resolve(basename + ".cpp");
		Path outputPath = OUTPUT_DIR.resolve(basename + ".cs");
		Files.writeString(inputPath, cpp, StandardCharsets.UTF_8);
		writer.write(inputPath, outputPath, cpp, result);
	}

	private void assertOutputFilesExist(String basename) {
		assertThat(OUTPUT_DIR.resolve(basename + ".cpp")).exists();
		assertThat(OUTPUT_DIR.resolve(basename + ".cs")).exists();
		assertThat(OUTPUT_DIR.resolve(basename + ".report.txt")).exists();
		assertThat(OUTPUT_DIR.resolve(basename + ".report.html")).exists();
		assertThat(OUTPUT_DIR.resolve(basename + ".treedump.txt")).exists();
		// xlsx は MAIN フェーズ（Transformer）が実行された場合のみ生成される。
		// comby のみのテストでは MAIN フェーズが空なため xlsx は生成されない。
	}
}
