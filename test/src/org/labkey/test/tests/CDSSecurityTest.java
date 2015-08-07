/*
 * Copyright (c) 2014-2015 LabKey Corporation
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
package org.labkey.test.tests;

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.CDS;
import org.labkey.test.util.CDSAsserts;
import org.labkey.test.util.CDSHelper;
import org.labkey.test.util.CDSInitializer;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PostgresOnlyTest;
import org.labkey.test.util.ReadOnlyTest;
import org.labkey.test.util.TestLogger;
import org.labkey.test.util.UIContainerHelper;
import org.omg.CORBA.SystemException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Category({CDS.class})
public class CDSSecurityTest extends BaseWebDriverTest implements PostgresOnlyTest, ReadOnlyTest
{

    private final CDSHelper cds = new CDSHelper(this);
    private final CDSAsserts _asserts = new CDSAsserts(this);

    private final String[] PERM_GROUPS = {"CDSSecurity Test Group01", "CDSSecurity Test Group02", "CDSSecurity Test Group03"};

    public void doSetup() throws Exception
    {
        CDSSecurityTest initTest = (CDSSecurityTest)getCurrentTest();
        CDSInitializer _initializer = new CDSInitializer(initTest, initTest.getProjectName());
        _initializer.setupDataspace();
    }

    @Override @LogMethod
    public boolean needsSetup()
    {
        boolean callDoCleanUp = false;

        try
        {
            if(HttpStatus.SC_NOT_FOUND == WebTestHelper.getHttpGetResponse(WebTestHelper.buildURL("project", getProjectName(), "begin")))
            {
                callDoCleanUp = false;
                doSetup();
            }

        }
        catch (IOException fail)
        {
            callDoCleanUp =  true;
        }
        catch(java.lang.Exception ex)
        {
            callDoCleanUp = true;
        }

        // Returning true will cause BaseWebDriver to call it's cleanup method.
        return callDoCleanUp;
    }

    @Before
    public void preTest()
    {
        Ext4Helper.setCssPrefix("x-");
        deletePermissionGroups();
        beginAt("project/" + getProjectName() + "/begin.view?");
    }

    @AfterClass
    public static void afterClassCleanUp()
    {
        CDSSecurityTest init = (CDSSecurityTest)getCurrentTest();
        init.deletePermissionGroups();
        Ext4Helper.resetCssPrefix();
    }

    @Override
    public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    public String getProjectName()
    {
        return CDSHelper.CDS_PROJECT_NAME;
    }

    @Override
    public List<String> getAssociatedModules()
    {
        return Arrays.asList("CDS");
    }

    @AfterClass
    public static void postTest()
    {
        Ext4Helper.resetCssPrefix();
    }

    @Test
    public void verifyUserPermissions()
    {

        ensureAdminMode();
        Ext4Helper.resetCssPrefix();

        _permissionsHelper.createPermissionsGroup(PERM_GROUPS[0]);
        if (isElementPresent(Locator.permissionRendered()) && isButtonPresent("Save and Finish"))
        {
            clickButton("Save and Finish");
        }

        clickProject(CDSHelper.CDS_PROJECT_NAME);
        clickFolder("v082"); // TODO Test data dependent.
        _permissionsHelper.enterPermissionsUI();
        sleep(1000);
        _permissionsHelper.uncheckInheritedPermissions();
        clickButton("Save", 0);

        //This is the workaround for issue 20329
        sleep(1000);
        _permissionsHelper.uncheckInheritedPermissions();
        clickButton("Save", 0);

        waitForElement(Locator.permissionRendered());

        _securityHelper.setProjectPerm(PERM_GROUPS[0], "Reader");
        clickButton("Save and Finish");

        clickProject(CDSHelper.CDS_PROJECT_NAME);
        _permissionsHelper.enterPermissionsUI();
        _securityHelper.setProjectPerm(PERM_GROUPS[0], "Reader");
        clickButton("Save and Finish");

        _permissionsHelper.createPermissionsGroup(PERM_GROUPS[1]);
        if (isElementPresent(Locator.permissionRendered()) && isButtonPresent("Save and Finish"))
        {
            clickButton("Save and Finish");
        }

        _permissionsHelper.enterPermissionsUI();
        _securityHelper.setProjectPerm(PERM_GROUPS[1], "Reader");
        clickButton("Save and Finish");

        impersonateGroup(PERM_GROUPS[0], false);

        cds.enterApplication();
        _asserts.assertFilterStatusCounts(8, 1, -1); // TODO Test data dependent.

        cds.viewLearnAboutPage("Studies");
        List<String> studies = Arrays.asList("HVTN 082"); // TODO Test data dependent.
        _asserts.verifyLearnAboutPage(studies);

        beginAt("project/" + getProjectName() + "/begin.view?");
        Ext4Helper.resetCssPrefix();
        clickUserMenuItem("Stop Impersonating");
        assertSignOutAndMyAccountPresent();

        impersonateGroup(PERM_GROUPS[1], false);

        cds.enterApplication();
        _asserts.assertFilterStatusCounts(0, 0, -1); // TODO Test data dependent.
        cds.viewLearnAboutPage("Studies");
        _asserts.verifyEmptyLearnAboutStudyPage();

        beginAt("project/" + getProjectName() + "/begin.view?");
        Ext4Helper.resetCssPrefix();
        clickUserMenuItem("Stop Impersonating");
        assertSignOutAndMyAccountPresent();

    }

    private void deletePermissionGroups()
    {
        String ExtDialogTitle;

        beginAt("project/" + getProjectName() + "/begin.view?");
        ensureAdminMode();
        Ext4Helper.resetCssPrefix();

        if (!isElementPresent(Locator.permissionRendered()))
        {
            _permissionsHelper.enterPermissionsUI();
        }

        _ext4Helper.clickTabContainingText("Project Groups");

        for(String group : PERM_GROUPS)
        {
            if (isTextPresent(group))
            {
                ExtDialogTitle = group + " Information";

                _permissionsHelper.openGroupPermissionsDisplay(group);
                _extHelper.waitForExtDialog(ExtDialogTitle);
                _permissionsHelper.deleteAllUsersFromGroup();
                clickButton("Delete Empty Group", 0);
                waitForElement(Locator.css(".groupPicker .x4-grid-cell-inner").withText("Users"), WAIT_FOR_JAVASCRIPT);
                clickButton("Cancel");

                if (!isElementPresent(Locator.permissionRendered()))
                {
                    _permissionsHelper.enterPermissionsUI();
                }

                _ext4Helper.clickTabContainingText("Project Groups");

            }

        }

        clickButton("Cancel");
    }

}
