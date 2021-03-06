<%
/*
 * Copyright (c) 2014-2017 LabKey Corporation
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
%>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.labkey.api.analytics.AnalyticsService" %>
<%@ page import="org.labkey.api.data.ContainerManager" %>
<%@ page import="org.labkey.api.security.User" %>
<%@ page import="org.labkey.api.settings.AppProps" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.PageConfig" %>
<%@ page import="org.labkey.cds.CDSController" %>
<%@ page import="org.labkey.cds.view.template.ConnectorTemplate" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    ConnectorTemplate me = (ConnectorTemplate) HttpView.currentView();
    CDSController.AppModel model = (CDSController.AppModel) me.getConnectorModel();
    PageConfig pageConfigBean = me.getModelBean(); // TODO make sure we pass in the page config when we create this template.
    String contextPath = request.getContextPath();
    String serverHash = PageFlowUtil.getServerSessionHash();
    String devModeParam = getActionURL().getParameter("devMode");
    boolean devMode = AppProps.getInstance().isDevMode() || (devModeParam != null && devModeParam.equalsIgnoreCase("1"));

    String appPath = contextPath + "/Connector";
    String sdkPath = contextPath + "/ext-4.2.1";
    String srcPath = appPath + "/src";
    String productionPath = contextPath + "/production/Connector";
    String resourcePath = productionPath + "/resources";
    String imageResourcePath = resourcePath + "/images";
    User user = getUser();
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <title>DataSpace</title>

    <link rel="icon" type="image/png" href="<%=text(appPath)%>/images/headerlogo.png">

    <!-- stylesheets -->
    <link type="text/css" href="<%=text(contextPath)%>/hopscotch/css/hopscotch.min.css" rel="stylesheet">
    <link type="text/css" href="<%=text(resourcePath)%>/Connector-all.css<%= text(devMode ? "" : ("?"+serverHash)) %>" rel="stylesheet">

    <!-- Include base labkey.js -->
    <%=PageFlowUtil.getLabkeyJS(getViewContext(), new LinkedHashSet<>(), false)%>
    <script type="text/javascript">
        var Connector = {
            studyContext: {
                schemaName: 'study',
                subjectColumn: LABKEY.moduleContext.study.subject.columnName,
                gridBaseSchema: 'cds',
                gridBase: 'GridBase',
                protocolDayColumn: 'ProtocolDay',
                subjectLabel: 'Subject Id'
            },
            resourceContext: {
                path: <%=PageFlowUtil.jsString(resourcePath)%>,
                imgPath: <%=PageFlowUtil.jsString(imageResourcePath)%>
            },
            user: {
                isAnalyticsUser: <%=model.isAnalyticsUser()%>,
                properties: <%=model.getUserProperties()%>
            }
        };

        Ext = {}; Ext4 = Ext;
    </script>
    <% if (pageConfigBean.getAllowTrackingScript())
    {
        String script = AnalyticsService.getTrackingScript();
        if (StringUtils.isNotEmpty(script))
        {
            if (user.isSiteAdmin())
            {
    %>      <!-- see <%=new ActionURL("analytics","begin",ContainerManager.getRoot()).getURIString()%> -->
    <%
            }
    %>
    <%=script%>
    <%
            }
        }
    %>

    <script type="text/javascript" src="<%=text(contextPath)%>/internal/jQuery/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/hopscotch/js/hopscotch.min.js"></script>

    <script type="text/javascript" src="<%=text(sdkPath)%>/ext-all<%= text(devMode ? "-debug" : "") %>.js"></script>
    <script type="text/javascript" src="<%=text(sdkPath)%>/ext-patches.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/ext-patches.js"></script>

    <!-- Client API Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/Ajax.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/Utils.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/dom/Utils.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/ActionURL.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/Filter.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/FieldKey.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/Query.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/Visualization.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/ParticipantGroup.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/core/Security.js"></script>

    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/Util.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/data/Reader.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/data/Proxy.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/data/Store.js"></script>

    <!-- Internal Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/dataregion/filter/Base.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/dataregion/filter/Model.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/dataregion/filter/Faceted.js"></script>

    <!-- Ext Widget Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/extWidgets/Ext4DefaultFilterPanel.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/extWidgets/Ext4GridPanel.js"></script>

    <!-- Visualization Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/d3-3.5.17.min.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/hexbin.min.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/sqbin.min.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/crossfilter-1.3.11.js"></script>

    <!-- LabKey Visualization Library -->
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/patches.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/utils.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/geom.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/stat.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/scale.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/layer.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/internal/D3Renderer.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/plot.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/MeasureStore.js"></script>

    <script type="text/javascript" src="<%=text(contextPath)%>/query/olap.js"></script>

    <% if (devMode) { %>

    <!-- CDS Module Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/Connector/cube.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/Connector/measure.js"></script>

    <!-- Connector Application -->
    <script type="text/javascript" src="<%=text(srcPath)%>/types/Filter.js"></script>

    <!-- Constant singletons -->
    <script type="text/javascript" src="<%=text(srcPath)%>/constant/ModuleViewsLookup.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/constant/State.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/constant/Templates.js"></script>

    <!-- Application Models -->
    <script type="text/javascript" src="<%=text(srcPath)%>/model/State.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/ColumnInfo.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Detail.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Dimension.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Explorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Filter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/FilterGroup.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Measure.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Source.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/InfoPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/InfoPaneMember.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/TimepointPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Summary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Group.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Grid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/MabDetail.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/MabGrid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/MabPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/MabSummary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/RSSItem.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Variable.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/VisitTag.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Antigen.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/ChartData.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/StudyAxisData.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/StudyVisitTag.js"></script>

    <!-- Application source -->
    <script type="text/javascript" src="<%=text(srcPath)%>/button/Image.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/button/RoundedButton.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/ActionTitle.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/AdvancedOption.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/AbstractAntigenSelection.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/AntigenSelection.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/MabVirusSelection.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/DropDown.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/GridPager.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/News.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/component/Started.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/AbstractFilter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/Filter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/Facet.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/AbstractGroupedFacet.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/MabGridFacet.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/LearnFacet.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/SystemMessage.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/Feedback.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/FilterPanel.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/Selection.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/GroupList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/Selector.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/HelpCenter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/grid/Panel.js"></script>

    <!-- Application plugins -->
    <script type="text/javascript" src="<%=text(srcPath)%>/plugin/Messaging.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/plugin/DocumentValidation.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/plugin/LoadingMask.js"></script>

    <!-- Factories -->
    <script type="text/javascript" src="<%=text(srcPath)%>/factory/Module.js"></script>

    <!-- Utilities -->
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/Animation.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/Statistics.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/StoreCache.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/Chart.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/HelpRouter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/Query.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/MabQuery.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/HashURL.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/FileExtension.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/PlotTooltip.js"></script>

    <!-- Application Stores -->
    <script type="text/javascript" src="<%=text(srcPath)%>/store/AssayDistinctValue.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/Explorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/FilterStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/MabStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/Summary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/VisitTag.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/StudyVisitTag.js"></script>

    <!-- Application Views -->
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Selection.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/DetailStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/FilterStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/InfoPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/GridPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/PlotPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/TimepointPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/GroupSave.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/GroupSummary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Header.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Home.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/HomeHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/HeaderDataView.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Learn.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/MabPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Main.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Navigation.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Page.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/PageHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Grid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/MabReport.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/MabGrid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/MabStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Variable.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/StudyAxis.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Chart.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/SingleAxisExplorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Summary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Viewport.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/view/module/BaseModule.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/module/Text.js"></script>

    <!-- Application Controllers -->
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/AbstractViewController.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/AbstractGridController.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Home.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Chart.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Connector.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Query.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/HttpInterceptor.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Messaging.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Filter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Analytics.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Explorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/FilterStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Group.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Learn.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Main.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Navigation.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Data.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/MabGrid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Router.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/State.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Summary.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/AssayAntigen.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Assay.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Labs.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Study.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/StudyProducts.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/VariableList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Report.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/PermissionedStudy.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/AssayAntigen.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Assay.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Labs.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Study.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/StudyProducts.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/VariableList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Report.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/LearnGrid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/LearnSummary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/AssayAntigen.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Assay.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Labs.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/ModuleContainer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Study.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/StudyProducts.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Report.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/ReportModuleContainer.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/DataAvailabilityModule.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/AssayAnalyteList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/AssayHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ContactCDS.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ProductHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ProductOtherProducts.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyPublications.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyProducts.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyRelationships.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyReports.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudySites.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/VariableList.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/Application.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app.js"></script>

    <% } else {  %>
    <!-- PRODUCTION -->
    <script type="text/javascript" src="<%=text(appPath)%>/extapp.min.js?v=<%=text(serverHash)%>"></script>
    <% } %>
</head>
<body>
<div class="banner" style="visibility: hidden;">
    <div class="banner-msg">Your session will expire in <span class="timer"></span>. Click anywhere to continue.</div>
</div>
<!-- BODY -->
<%  me.include(me.getBody(),out); %>
<!-- /BODY -->
</body>
</html>
