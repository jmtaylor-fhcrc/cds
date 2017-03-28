/*
 * Copyright (c) 2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.test.tests.cds;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.pages.cds.XAxisVariableSelector;
import org.labkey.test.pages.cds.YAxisVariableSelector;
import org.labkey.test.util.ApiPermissionsHelper;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.cds.CDSAsserts;
import org.labkey.test.util.cds.CDSHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category({})
public class CDSGroupTest extends CDSReadOnlyTest
{

    public ApiPermissionsHelper _apiPermissionsHelper = new ApiPermissionsHelper(this);

    // Known Test Groups
    private static final String GROUP_LIVE_FILTER = "CDSTest_DGroup";
    private static final String GROUP_STATIC_FILTER = "CDSTest_EGroup";
    private static final String STUDY_GROUP = "Study Group Verify";
    private static final String SHARED_GROUP_NAME = "shared_Group";
    private static final String GROUP_PLOT_TEST = "Group Plot Test";

    private static final String HOME_PAGE_GROUP = "A Plotted Group For Home Page Verification and Testing.";

    private final CDSHelper cds = new CDSHelper(this);
    private final CDSAsserts _asserts = new CDSAsserts(this);

    @Before
    public void preTest()
    {

        cds.enterApplication();

        // clean up groups
        cds.goToAppHome();
        sleep(CDSHelper.CDS_WAIT_ANIMATION); // let the group display load

        List<String> groups = new ArrayList<>();
        groups.add(GROUP_LIVE_FILTER);
        groups.add(GROUP_STATIC_FILTER);
        groups.add(STUDY_GROUP);
        groups.add(HOME_PAGE_GROUP);
        groups.add(SHARED_GROUP_NAME);
        cds.ensureGroupsDeleted(groups);

        cds.ensureNoFilter();
        cds.ensureNoSelection();

        // go back to app starting location
        cds.goToAppHome();
    }

    @Override
    public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("CDS");
    }

    @Test
    public void verifyGroups()
    {
        log("Verify Groups");

        //
        // Define Group Names
        //
        String studyGroupDesc = "A set of defined studies.";
        String studyGroupDescModified = "A set of defined studies. More info added.";

        //
        // Compose Groups
        //
        cds.goToSummary();
        cds.clickBy("Studies");
        cds.selectBars(CDSHelper.STUDIES[0], CDSHelper.STUDIES[1]);
        cds.useSelectionAsSubjectFilter();
        cds.saveGroup(STUDY_GROUP, studyGroupDesc);

        // verify group save messaging
        //ISSUE 19997
        waitForText("Group \"Study Group...\" saved.");

        // verify filter is still applied
        assertElementPresent(CDSHelper.Locators.filterMemberLocator(CDSHelper.STUDIES[0]));
        assertElementPresent(CDSHelper.Locators.filterMemberLocator(CDSHelper.STUDIES[1]));

        // verify group can be updated
        click(CDSHelper.Locators.cdsButtonLocator("save", "filtersave"));
        waitForText("replace an existing group");
        click(CDSHelper.Locators.cdsButtonLocator("replace an existing group"));

        Locator.XPathLocator listGroup = Locator.tagWithClass("div", "save-label");
        waitAndClick(listGroup.withText(STUDY_GROUP));

        setFormElement(Locator.id("updategroupdescription-inputEl"), studyGroupDescModified);
        click(CDSHelper.Locators.cdsButtonLocator("Save", "groupupdatesave"));

        // verify group save messaging
        waitForText("Group \"Study Group...\" saved.");
        _asserts.assertFilterStatusCounts(89, 2, 1, 3, 7); // TODO Test data dependent.

        CDSHelper.NavigationLink.HOME.makeNavigationSelection(this);
        waitForText(STUDY_GROUP);
        click(Locator.tagWithClass("div", "grouplabel").withText(STUDY_GROUP));

        // Verify that the description has changed.
        waitForText(studyGroupDescModified);

        // Verify that No plot data message is shown
        assertTextPresent("No plot saved for this group.");

        // verify 'whoops' case
        click(CDSHelper.Locators.cdsButtonLocator("save", "filtersave"));
        waitForText("create a new group");
        click(CDSHelper.Locators.cdsButtonLocator("Cancel", "groupcancelreplace"));
        cds.clearFilters();

        // add a filter, which should be blown away when a group filter is selected
        cds.goToSummary();
        cds.clickBy("Assays");
        cds.selectBars(CDSHelper.ASSAYS[1]);
        cds.useSelectionAsSubjectFilter();
        _asserts.assertFilterStatusCounts(1604, 14, 2, 1, 91); // TODO Test data dependent.

        CDSHelper.NavigationLink.HOME.makeNavigationSelection(this);
        waitForText(STUDY_GROUP);
        click(Locator.tagWithClass("div", "grouplabel").withText(STUDY_GROUP));

        // Verify the group does overwrite already active filters
        sleep(500); // give it a chance to apply
        assertElementNotPresent(CDSHelper.Locators.filterMemberLocator(CDSHelper.ASSAYS[1]));
        cds.clearFilters();

        // Verify the filters get applied when directly acting
        CDSHelper.NavigationLink.HOME.makeNavigationSelection(this);
        waitForText(STUDY_GROUP);
        click(Locator.tagWithClass("div", "grouplabel").withText(STUDY_GROUP));
        waitForElement(CDSHelper.Locators.filterMemberLocator(CDSHelper.STUDIES[0]));
        assertElementNotPresent(CDSHelper.Locators.filterMemberLocator(CDSHelper.ASSAYS[1]));
        _asserts.assertFilterStatusCounts(89, 2, 1, 3, 7); // TODO Test data dependent.
        assertTextPresent("Study Group Verify", "Description", studyGroupDescModified);
        cds.clearFilters();

        // Verify that you can cancel delete
        click(CDSHelper.Locators.cdsButtonLocator("Delete"));
        waitForText("Are you sure you want to delete");
        click(CDSHelper.Locators.cdsButtonLocator("Cancel", "x-toolbar-item").notHidden());
        _ext4Helper.waitForMaskToDisappear();
        assertTextPresent(studyGroupDescModified);

        // Verify back button works
        click(CDSHelper.Locators.pageHeaderBack());
        waitForText(CDSHelper.HOME_PAGE_HEADER);
        waitForText(STUDY_GROUP);

        // Verify delete works.
        cds.deleteGroupFromSummaryPage(STUDY_GROUP);

        cds.clearFilters();

        //Compose new Group
        cds.goToSummary();
        cds.clickBy("Studies");
        cds.selectBars(CDSHelper.STUDIES[0], CDSHelper.STUDIES[1]);
        cds.useSelectionAsSubjectFilter();

        CDSHelper.NavigationLink.PLOT.makeNavigationSelection(this);

        XAxisVariableSelector xaxis = new XAxisVariableSelector(this);
        YAxisVariableSelector yaxis = new YAxisVariableSelector(this);

        xaxis.openSelectorWindow();
        xaxis.pickSource(CDSHelper.TIME_POINTS);
        xaxis.pickVariable(CDSHelper.TIME_POINTS_DAYS);
        xaxis.confirmSelection();
        sleep(CDSHelper.CDS_WAIT_ANIMATION);
        yaxis.pickSource(CDSHelper.NAB);
        yaxis.pickVariable(CDSHelper.NAB_TITERIC50);
        yaxis.confirmSelection();

        cds.saveGroup(GROUP_PLOT_TEST, "a plot", false, true);

        cds.clearFilters(true);

        CDSHelper.NavigationLink.HOME.makeNavigationSelection(this);
        waitForText(GROUP_PLOT_TEST );
        click(Locator.tagWithClass("div", "grouplabel").withText(GROUP_PLOT_TEST ));
        waitForText("View in Plot");
        click(Locator.xpath("//span/following::span[contains(text(), 'View in Plot')]").parent().parent().parent());
        // Simply verify that we are on the plot page. Other tests validate plot functionality.
        assertTrue(getDriver().getCurrentUrl().contains("#chart"));
        CDSHelper.NavigationLink.HOME.makeNavigationSelection(this);
        cds.deleteGroupFromSummaryPage(GROUP_PLOT_TEST );
    }

    @Test
    public void verifySharedGroups()
    {
        final String[] NEW_USER_ACCOUNTS = {"cds_alice@example.com", "cds_bob@example.com", "cds_eve@example.com"};
        //this test case focuses on whether groups are shared properly.
        final String[] PRIVATE_GROUP_NAME = {"test_Group_reader", "test_Group_editor"};
        final String[] PRIVATE_GROUP_NAME_DESCRIPTION = {"This group selects two studies", "This group selects two studies"};

        final Locator SHARED_GROUP_LOC = Locator.xpath("//*[contains(@class, 'section-title')][contains(text(), 'Curated groups and plots')]" +
                "/following::div[contains(@class, 'grouprow')]/div[contains(text(), '" + SHARED_GROUP_NAME + "')]");


        //Ensure test users don't already exist
        _userHelper.deleteUser(NEW_USER_ACCOUNTS[0]);
        _userHelper.deleteUser(NEW_USER_ACCOUNTS[1]);
        _userHelper.deleteUser(NEW_USER_ACCOUNTS[2]);

        log("Testing permissions for creating a shared group");
        //Validate a user with Reader role can create a group without issue.
        _impersonateRole("Reader");
        //Create a group.
        _composeGroup();
        //saveGroup verifies that the shared group checkbox is not present.
        boolean result = cds.saveGroup(PRIVATE_GROUP_NAME[0], PRIVATE_GROUP_NAME_DESCRIPTION[0], true);
        assertFalse("Updating shared status of group should fail.", result);
        result = cds.saveGroup(PRIVATE_GROUP_NAME[0], PRIVATE_GROUP_NAME_DESCRIPTION[0], false);
        assertTrue("Failed to update group", result);
        _stopImpersonatingRole();

        _impersonateRole("Editor");
        _composeGroup();
        result = cds.saveGroup(PRIVATE_GROUP_NAME[1], PRIVATE_GROUP_NAME_DESCRIPTION[1], true);
        assertTrue("Failed to create new shared group as Editor.", result);
        _stopImpersonatingRole();

        cds.deleteGroupFromSummaryPage(PRIVATE_GROUP_NAME[0]);
        cds.deleteGroupFromSummaryPage(PRIVATE_GROUP_NAME[1]);

        String rootContainer = getProjectName();

        _userHelper.createUser(NEW_USER_ACCOUNTS[0], false, true);
        _userHelper.createUser(NEW_USER_ACCOUNTS[1], false, true);
        _userHelper.createUser(NEW_USER_ACCOUNTS[2], false, true);

        goToProjectHome();

        Ext4Helper.resetCssPrefix();
        _apiPermissionsHelper.setUserPermissions(NEW_USER_ACCOUNTS[0], "Editor");
        _apiPermissionsHelper.setUserPermissions(NEW_USER_ACCOUNTS[1], "Reader");
        _apiPermissionsHelper.setUserPermissions(NEW_USER_ACCOUNTS[2], "Editor");

        //Arbitrary amount of studies to run through
        for(int itr = 0; itr < 5; itr++)
        {
            String studyName = CDSHelper.PROTS[itr];
            goToProjectHome(rootContainer + "/" + studyName);
            _apiPermissionsHelper.setUserPermissions(NEW_USER_ACCOUNTS[0], "Editor");
            _apiPermissionsHelper.setUserPermissions(NEW_USER_ACCOUNTS[1], "Reader");
            _apiPermissionsHelper.setUserPermissions(NEW_USER_ACCOUNTS[2], "Editor");
        }
        Ext4Helper.setCssPrefix("x-");

        //As an editor, make a shared group and a private group
        _impersonateUser(NEW_USER_ACCOUNTS[0]);
        _composeGroup();
        cds.saveGroup(PRIVATE_GROUP_NAME[0], PRIVATE_GROUP_NAME_DESCRIPTION[0], false);
        cds.saveGroup(SHARED_GROUP_NAME, "", true);
        _stopImpersonatingRole();


        //Impersonate the reader
        _impersonateUser(NEW_USER_ACCOUNTS[1]);
        cds.enterApplication();

        //Verify that private group is not shared and that public group is
        Locator mineHeader = Locator.xpath("//h2[contains(text(), 'My saved groups and plots')][contains(@class, 'section-title')]");
        assertElementNotPresent("User should not have any of their own groups.", mineHeader);
        assertElementNotPresent(PRIVATE_GROUP_NAME[0] + " should not been visible to this user",
                Locator.xpath("//div[contains(@class, 'grouplabel')][contains(text(), '" + PRIVATE_GROUP_NAME[0] + "')]"));
        assertTrue("Shared group should be visible", isElementPresent(SHARED_GROUP_LOC));

        //Examine shared group
        click(SHARED_GROUP_LOC);
        waitForText("Edit details");

        //verify that reader cannot edit
        click(CDSHelper.Locators.cdsButtonLocator("Edit details"));
        click(CDSHelper.Locators.cdsButtonLocator("Save").notHidden());
        waitForText("Failed to edit Group");
        click(CDSHelper.Locators.cdsButtonLocator("OK", "x-toolbar-item").notHidden());
        _ext4Helper.waitForMaskToDisappear();

        //Verify that reader cannot delete
        click(CDSHelper.Locators.cdsButtonLocator("Delete"));
        waitForText("Are you sure you want to delete");
        click(CDSHelper.Locators.cdsButtonLocator("Delete", "x-toolbar-item").notHidden());
        waitForText("ERROR");
        click(CDSHelper.Locators.cdsButtonLocator("OK", "x-toolbar-item").notHidden());

        //switch to other editor account
        _stopImpersonatingUser();
        _impersonateUser(NEW_USER_ACCOUNTS[2]);
        cds.enterApplication();

        //verify that another editor can update shared group
        cds.goToSummary();
        cds.clickBy("Studies");
        cds.selectBars(CDSHelper.STUDIES[3], CDSHelper.STUDIES[4]);
        cds.useSelectionAsSubjectFilter();
        boolean updateSuccess = cds.updateSharedGroupDetails(SHARED_GROUP_NAME, null, "Updated Description", null);
        assertTrue("Expected to successfully update group description", updateSuccess);

        assertTrue("Filter was not correctly updated", isElementPresent(
                Locator.xpath("//div[contains(@class, 'sel-list-item')][contains(text(), '"
                        + CDSHelper.STUDIES[0] + ", " + CDSHelper.STUDIES[1] + "')]")));

        updateSuccess = cds.updateSharedGroupDetails(SHARED_GROUP_NAME, null, null, false); //should fail
        assertFalse("Expected to fail group update. Should not be able to unshared other user's group", updateSuccess);

        //delete group
        click(SHARED_GROUP_LOC);
        waitForText("Edit details");
        click(CDSHelper.Locators.cdsButtonLocator("Delete"));
        waitForText("Are you sure you want to delete");
        click(CDSHelper.Locators.cdsButtonLocator("Delete", "x-toolbar-item").notHidden());
        waitForText("Groups and plots");
        refresh();
        assertElementNotPresent("Group: " + SHARED_GROUP_NAME + " should not have been present after deletion",
                Locator.xpath("//*[contains(@class, 'section-title')]" +
                        "[contains(text(), 'Curated groups and plots')]" +
                        "/following::div[contains(@class, 'grouprow')]" +
                        "/div[contains(text(), '" + SHARED_GROUP_NAME + "')]"));
        _stopImpersonatingUser();

        _userHelper.deleteUser(NEW_USER_ACCOUNTS[0]);
        _userHelper.deleteUser(NEW_USER_ACCOUNTS[1]);
        _userHelper.deleteUser(NEW_USER_ACCOUNTS[2]);

    }

    private void _composeGroup()
    {
        cds.goToSummary();
        cds.clickBy("Studies");
        cds.selectBars(CDSHelper.STUDIES[0], CDSHelper.STUDIES[1]);
        cds.useSelectionAsSubjectFilter();
    }

    private void _impersonateRole(String role)
    {
        doActionInStandardLabkey(() -> impersonateRole(role));
    }

    private void _stopImpersonatingRole()
    {
        doActionInStandardLabkey(this::stopImpersonatingRole);
    }

    private void _impersonateUser(String user)
    {
        doActionInStandardLabkey(() -> impersonate(user));
    }

    private void _stopImpersonatingUser()
    {
        doActionInStandardLabkey(this::stopImpersonating);
    }

    private void doActionInStandardLabkey(Runnable action)
    {
        goToProjectHome();
        Ext4Helper.resetCssPrefix();
        action.run();
        Ext4Helper.setCssPrefix("x-");
        cds.enterApplication();
    }

}
