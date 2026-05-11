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

import io.github.takahino.cpp2csharp.matcher.CppParserFactory;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader;
import io.github.takahino.cpp2csharp.rule.ConversionRuleLoader.ThreePassRuleSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PatternDiscoveryEngine} のユニットテスト。
 */
class PatternDiscoveryEngineTest {

	private final PatternDiscoveryEngine engine = new PatternDiscoveryEngine();

	// -------------------------------------------------------------------------
	// GLOBAL_FUNC 検出
	// -------------------------------------------------------------------------

	@Test
	void globalFunc_detectedWhenNotPrecededByDotOrArrow() throws IOException {
		Path file = writeTempFile("test.cpp", "void f() { AfxMessageBox(\"Hello\"); }");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ)
				.anyMatch(o -> o.type() == PatternType.GLOBAL_FUNC && o.identifierName().equals("AfxMessageBox"));
	}

	@Test
	void globalFunc_notDetectedWhenPrecededByDot() throws IOException {
		Path file = writeTempFile("test.cpp", "obj.Format(\"%d\", x);");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).noneMatch(o -> o.type() == PatternType.GLOBAL_FUNC && o.identifierName().equals("Format"));
	}

	@Test
	void globalFunc_notDetectedWhenPrecededByArrow() throws IOException {
		Path file = writeTempFile("test.cpp", "ptr->GetValue(x);");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).noneMatch(o -> o.type() == PatternType.GLOBAL_FUNC && o.identifierName().equals("GetValue"));
	}

	@Test
	void globalFunc_notDetectedForClassMethodDefinition() throws IOException {
		// ClassName::MethodName( は自クラス定義のため GLOBAL_FUNC に含めない
		Path file = writeTempFile("test.cpp", "void MyClass::MyMethod(int x) {}");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).noneMatch(o -> o.type() == PatternType.GLOBAL_FUNC && o.identifierName().equals("MyMethod"));
	}

	// -------------------------------------------------------------------------
	// METHOD_CALL 検出
	// -------------------------------------------------------------------------

	@Test
	void methodCall_detectedForDotOperator() throws IOException {
		Path file = writeTempFile("test.cpp", "str.Left(5);");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).anyMatch(o -> o.type() == PatternType.METHOD_CALL && o.identifierName().equals("Left")
				&& ".".equals(o.accessOperator()));
	}

	@Test
	void methodCall_detectedForArrowOperator() throws IOException {
		Path file = writeTempFile("test.cpp", "pWnd->ShowWindow(SW_SHOW);");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).anyMatch(o -> o.type() == PatternType.METHOD_CALL && o.identifierName().equals("ShowWindow")
				&& "->".equals(o.accessOperator()));
	}

	// -------------------------------------------------------------------------
	// TYPE_NAME 検出
	// -------------------------------------------------------------------------

	@Test
	void typeName_detectedForKnownMfcType() throws IOException {
		Path file = writeTempFile("test.cpp", "CString str;");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).anyMatch(o -> o.type() == PatternType.TYPE_NAME && o.identifierName().equals("CString"));
	}

	@Test
	void typeName_notDetectedForUnknownType() throws IOException {
		Path file = writeTempFile("test.cpp", "MyCustomType x;");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).noneMatch(o -> o.type() == PatternType.TYPE_NAME);
	}

	// -------------------------------------------------------------------------
	// 引数カウント
	// -------------------------------------------------------------------------

	@Test
	void countArgs_zeroForEmptyParens() throws IOException {
		Path file = writeTempFile("test.cpp", "Foo();");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).anyMatch(o -> o.identifierName().equals("Foo") && o.argCount() == 0);
	}

	@Test
	void countArgs_oneForSingleArg() throws IOException {
		Path file = writeTempFile("test.cpp", "Foo(x);");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).anyMatch(o -> o.identifierName().equals("Foo") && o.argCount() == 1);
	}

	@Test
	void countArgs_correctForNestedParens() throws IOException {
		Path file = writeTempFile("test.cpp", "Foo(bar(a, b), c);");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		assertThat(occ).anyMatch(o -> o.identifierName().equals("Foo") && o.argCount() == 2);
	}

	// -------------------------------------------------------------------------
	// discover 結合テスト
	// -------------------------------------------------------------------------

	@Test
	void discover_aggregatesAcrossFiles(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("a.cpp"), "AfxMessageBox(\"A\");", StandardCharsets.UTF_8);
		Files.writeString(tempDir.resolve("b.cpp"), "AfxMessageBox(\"B\"); AfxMessageBox(\"C\");",
				StandardCharsets.UTF_8);

		ThreePassRuleSet emptyRuleSet = new ThreePassRuleSet(List.of(), List.of(), List.of(), List.of(), List.of());
		PatternDiscoveryResult result = engine.discover(tempDir, emptyRuleSet);

		assertThat(result.allPatterns()).anyMatch(p -> p.identifierName().equals("AfxMessageBox")
				&& p.occurrenceCount() == 3 && p.occurrenceFiles().size() == 2);
	}

	@Test
	void discover_classifiesRuleCoverage(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("test.cpp"), "AfxMessageBox(\"Hello\");", StandardCharsets.UTF_8);

		// AfxMessageBox をカバーするルールセットを読み込む
		ThreePassRuleSet ruleSet = new ConversionRuleLoader(CppParserFactory.asLexerFactory()).loadThreePassRules();
		PatternDiscoveryResult result = engine.discover(tempDir, ruleSet);

		// AfxMessageBox はルールが存在するはず
		assertThat(result.coveredPatterns()).anyMatch(p -> p.identifierName().equals("AfxMessageBox"));
	}

	// -------------------------------------------------------------------------
	// 引数数ごとの区別
	// -------------------------------------------------------------------------

	@Test
	void methodCall_distinguishedByArgCount() throws IOException {
		// Format(1引数) と Format(2引数) は別パターンとして集約される
		Path file = writeTempFile("test.cpp", "str.Format(\"%d\"); str.Format(\"%d\", x); str.Format(\"%d\", x);");
		List<PatternDiscoveryEngine.RawOccurrence> occ = engine.scanFile(file);

		long arg1count = occ.stream().filter(o -> o.identifierName().equals("Format") && o.argCount() == 1).count();
		long arg2count = occ.stream().filter(o -> o.identifierName().equals("Format") && o.argCount() == 2).count();

		assertThat(arg1count).isEqualTo(1);
		assertThat(arg2count).isEqualTo(2);
	}

	// -------------------------------------------------------------------------
	// ヘルパー
	// -------------------------------------------------------------------------

	private Path writeTempFile(String name, String content) throws IOException {
		Path tmp = Files.createTempFile(name.replace(".cpp", ""), ".cpp");
		tmp.toFile().deleteOnExit();
		Files.writeString(tmp, content, StandardCharsets.UTF_8);
		return tmp;
	}
}
