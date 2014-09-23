<%
/*
 * Copyright (c) 2014 LabKey Corporation
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
<%@ page import="org.labkey.api.settings.AppProps" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.api.view.template.PrintTemplate" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    PrintTemplate me   = (PrintTemplate) HttpView.currentView();
    String contextPath = request.getContextPath();
    String serverHash = PageFlowUtil.getServerSessionHash();
    String devModeParam = getActionURL().getParameter("devMode");
    Boolean devMode = AppProps.getInstance().isDevMode() || (devModeParam != null && devModeParam.equalsIgnoreCase("1"));

    String appPath = contextPath + "/Connector";
    String sdkPath = contextPath + "/ext-4.2.1";
    String srcPath = appPath + "/src";
    String productionPath = contextPath + "/production/Connector";
    String resourcePath = productionPath + "/resources";
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <title>HIV CDS</title>

    <link rel="icon" type="image/png" href="<%=text(appPath)%>/images/logo_02.png">
    <link type="text/css" href="<%=text(resourcePath)%>/Connector-all.css<%= text(devMode ? "" : ("?"+serverHash)) %>" rel="stylesheet">

    <!-- Include base labkey.js -->
    <%=PageFlowUtil.getLabkeyJS(getViewContext(), new LinkedHashSet<ClientDependency>())%>
    <script type="text/javascript">
        var Connector = {
            studyContext: {
                subjectColumn: LABKEY.moduleContext.study.subject.columnName,
                subjectVisitColumn: 'SubjectVisit'
            },
            resourceContext: {
                path: <%=PageFlowUtil.jsString(resourcePath)%>
            }
        };

        Ext = {}; Ext4 = Ext;
    </script>
    <script src="https://maps.googleapis.com/maps/api/js?sensor=false"></script>

    <% if (devMode) { %>
    <script type="text/javascript" src="<%=text(sdkPath)%>/ext-all<%= text(devMode ? "-debug" : "") %>.js"></script>
    <script type="text/javascript" src="<%=text(sdkPath)%>/ext-patches.js"></script>

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

    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/Util.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/data/Reader.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/data/Proxy.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/clientapi/ext4/data/Store.js"></script>

    <!-- Internal Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/dataregion/filter/Base.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/dataregion/filter/Model.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/dataregion/filter/Faceted.js"></script>

    <!-- Study Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/study/MeasurePicker.js"></script>

    <!-- App API Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/app/State.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/View.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/Route.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/Filter.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/Selection.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/AbstractViewController.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/OlapExplorer.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/MeasurePicker.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/app/LoadingMask.js"></script>

    <!-- Ext Widget Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/extWidgets/Ext4DefaultFilterPanel.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/extWidgets/Ext4GridPanel.js"></script>

    <!-- Visualization Dependencies -->
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/d3-3.3.9.min.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/raphael-min-2.1.0.js"></script>

    <!-- LabKey Visualization Library -->
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/lib/patches.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/utils.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/geom.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/stat.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/scale.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/layer.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/internal/RaphaelRenderer.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/internal/D3Renderer.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/vis/src/plot.js"></script>

    <script type="text/javascript" src="<%=text(contextPath)%>/query/olap.js"></script>
    <script type="text/javascript" src="<%=text(contextPath)%>/Connector/cube.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/types/Filter.js"></script>

    <!-- Application Models -->
    <script type="text/javascript" src="<%=text(srcPath)%>/model/ColumnInfo.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Detail.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Dimension.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Explorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Filter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/FilterGroup.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/InfoPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Summary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Group.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Grid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/RSSItem.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Variable.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/VisitTag.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/model/Antigen.js"></script>

    <!-- Application source -->
    <script type="text/javascript" src="<%=text(srcPath)%>/button/Image.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/button/RoundedButton.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/AbstractFilter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/Filter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/Facet.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/window/SystemMessage.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/AxisSelector.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/Feedback.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/FilterPanel.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/Selection.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/panel/GroupList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/grid/Panel.js"></script>

    <!-- Application plugins -->
    <script type="text/javascript" src="<%=text(srcPath)%>/plugin/Messaging.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/plugin/LoadingMask.js"></script>

    <!-- Constant singletons -->
    <script type="text/javascript" src="<%=text(srcPath)%>/constant/Templates.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/constant/ModuleViewsLookup.js"></script>

    <!-- Factories -->
    <script type="text/javascript" src="<%=text(srcPath)%>/factory/Module.js"></script>

    <!-- Application Stores -->
    <script type="text/javascript" src="<%=text(srcPath)%>/store/AssayDistinctValue.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/Explorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/FilterStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/Summary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/store/VisitTag.js"></script>

    <!-- Application Views -->
    <script type="text/javascript" src="<%=text(srcPath)%>/view/About.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Compare.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Selection.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/DetailStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/FilterStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/InfoPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/GridPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/PlotPane.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/GroupSave.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/GroupSummary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Header.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Home.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Learn.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Main.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Navigation.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Page.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/PageHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Popup.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Grid.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Variable.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/StudyAxis.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Scatter.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Signin.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/SingleAxisExplorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Summary.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/TermsOfUse.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Time.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/Viewport.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/view/module/BaseModule.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/module/ContactCDS.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/module/Text.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/view/module/Person.js"></script>

    <!-- Application Controllers -->
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/AbstractViewController.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Home.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Chart.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Connector.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Query.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Explorer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/FilterStatus.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Group.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Learn.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Main.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Navigation.js"></script>
    
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Data.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Router.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Signin.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/State.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/controller/Summary.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/utility/Animation.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/Statistics.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/utility/StoreCache.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Assay.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/DataSet.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/DataSetData.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Labs.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Site.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/Study.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/model/StudyProducts.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Assay.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/DataSet.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/DataSetData.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Labs.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Site.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/Study.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/store/StudyProducts.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Assay.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Labs.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/ModuleContainer.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Site.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/Study.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/StudyProducts.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/AssayAnalyteList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/AssayAntigenList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/AssayHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/AssayVariableList.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ProductHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ProductManufacturing.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ProductProvidedBy.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ProductOtherProducts.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/ProductStudies.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyAssays.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyDataSets.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyHeader.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudyProducts.js"></script>
    <script type="text/javascript" src="<%=text(srcPath)%>/app/view/module/StudySites.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/Application.js"></script>

    <script type="text/javascript" src="<%=text(srcPath)%>/app.js"></script>

    <% } else {  %>
    <!-- PRODUCTION -->
    <script type="text/javascript" src="<%=text(appPath)%>/extapp.min.js?v=<%=text(serverHash)%>"></script>
    <% } %>
</head>
<body>
<!-- BODY -->
<%  me.include(me.getBody(),out); %>
<!-- /BODY -->
</body>
</html>
