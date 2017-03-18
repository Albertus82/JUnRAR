/*
 * Copyright (c) 2007 innoSysTec (R) GmbH, Germany. All rights reserved.
 * Original author: EW
 * Creation date: 26.09.2007
 *
 * Source: $HeadURL$
 * Last changed: $LastChangedDate$
 *
 *
 * the unrar licence applies to all junrar source and binary distributions
 * you are not allowed to use this source to re-create the RAR compression algorithm
 *
 * Here some html entities which can be used for escaping javadoc tags:
 * "&":  "&#038;" or "&amp;"
 * "<":  "&#060;" or "&lt;"
 * ">":  "&#062;" or "&gt;"
 * "@":  "&#064;"
 */
package junrar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import junrar.exception.RarException;

/**
 * DOCUMENT ME
 *
 * @author $LastChangedBy$
 * @version $LastChangedRevision$
 */
public class JUnRarTest {

	private static final Log logger = LogFactory.getLog(JUnRarTest.class);

	private static List<String> successfulFiles = new ArrayList<String>();
	private static List<String> errorFiles = new ArrayList<String>();

	private volatile static File tempFolder;

	@Test
	public void bestLock() throws IOException, RarException {
		testArchive("best_lock.rar");
	}

	@Test
	public void bestSolidMultiOld() throws IOException, RarException {
		testArchive("best_solid_multi_old.rar", "best_solid_multi_old.r00", "best_solid_multi_old.r01");
	}

	@Test
	public void best() throws IOException, RarException {
		testArchive("best.rar");
	}

	@Test
	public void fastestRecoveryLockMultiNew() throws IOException, RarException {
		testArchive("fastest_recovery_lock_multi_new.part1.rar", "fastest_recovery_lock_multi_new.part2.rar", "fastest_recovery_lock_multi_new.part3.rar");
	}

	@Test
	public void normalSolidLock() throws IOException, RarException {
		testArchive("normal_solid_lock.rar");
	}

	@Test
	public void storeRecovery() throws IOException, RarException {
		testArchive("store_recovery.rar");
	}

//	@Test
//	public void goodProtectedSolid() throws IOException, RarException {
//		testSecuredArchive("letteratura", "good_protected_solid_lock.rar");
//	}
//
//	@Test
//	public void bestEncryptedLock() throws IOException, RarException {
//		testSecuredArchive("letteratura", "best_encrypted_lock.rar");
//	}
//
//	@Test
//	public void bestSolidEncryptedLockRecoveryMulti() throws IOException, RarException {
//		testSecuredArchive("letteratura", "best_solid_encrypted_lock_recovery_multi.part1.rar", "best_solid_encrypted_lock_recovery_multi.part2.rar", "best_solid_encrypted_lock_recovery_multi.part3.rar");
//	}

	@Test
	public void corrupted() throws IOException, RarException {
		try {
			testArchive("corrupted.rar");
			assertFalse(true);
		}
		catch (Exception re) {
			assertTrue(true);
		}
	}

	@After
	public void deleteTempFolder() {
		FileUtils.deleteQuietly(tempFolder);
	}

	private void testArchive(final String... fileNames) throws IOException, RarException {
		testSecuredArchive(null, fileNames);
	}

	private synchronized void testSecuredArchive(final String password, final String... fileNames) throws IOException, RarException {
		tempFolder = File.createTempFile("junrar_", '_' + fileNames[0]);
		FileUtils.deleteQuietly(tempFolder);
		tempFolder.mkdir();

		File firstVolume = new File(tempFolder, fileNames[0]);
		for (String fileName : fileNames) {
			InputStream resourceAsStream = this.getClass().getResourceAsStream(fileName);
			File testRar = new File(tempFolder, fileName);
			FileUtils.writeByteArrayToFile(testRar, IOUtils.toByteArray(resourceAsStream));
			resourceAsStream.close();
		}

		InputStream is1 = null;
		InputStream is2 = null;
		try {
			ExtractArchive.main(new String[] { firstVolume.getAbsolutePath(), tempFolder.getAbsolutePath(), password });

			/* Verifica presenza file e directory estratti */
			File extractedDir = new File(tempFolder, "dante");
			assertTrue(extractedDir.exists());
			File extractedFile = new File(extractedDir, "comedia.txt");
			assertTrue(extractedFile.exists());

			is1 = this.getClass().getResourceAsStream("comedia.txt");
			String originalText = IOUtils.toString(is1, "UTF-8");
			is1.close();
			String extractedText = FileUtils.readFileToString(extractedFile, "UTF-8");
			assertEquals(originalText, extractedText);

			extractedDir = new File(tempFolder, "petrarca");
			assertTrue(extractedDir.exists());
			extractedFile = new File(extractedDir, "canzoniere.txt");
			assertTrue(extractedFile.exists());

			is2 = this.getClass().getResourceAsStream("canzoniere.txt");
			originalText = IOUtils.toString(is2, "UTF-8");
			is2.close();
			extractedText = FileUtils.readFileToString(extractedFile, "UTF-8");
			assertEquals(originalText, extractedText);
		}
		catch (RarException re) {
			errorFiles.add(fileNames[0]);
			throw re;
		}
		catch (AssertionError ae) {
			errorFiles.add(fileNames[0]);
			throw ae;
		}
		finally {
			IOUtils.closeQuietly(is1);
			IOUtils.closeQuietly(is2);
		}
		successfulFiles.add(fileNames[0]);
	}

	@AfterClass
	public static void tearDownFunctionalTests() throws IOException {
		logger.info(successfulFiles.size() + " RAR OK: " + successfulFiles.toString());
		logger.info(errorFiles.size() + " RAR KO: " + errorFiles.toString());
	}

}
