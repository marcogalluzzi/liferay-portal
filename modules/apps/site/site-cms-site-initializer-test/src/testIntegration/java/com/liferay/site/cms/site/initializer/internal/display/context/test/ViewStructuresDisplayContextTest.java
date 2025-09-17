/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.cms.site.initializer.internal.display.context.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.util.LinkedHashMapBuilder;
import com.liferay.portal.test.rule.FeatureFlag;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Marco Galluzzi
 */
@FeatureFlag("LPD-17564")
@RunWith(Arquillian.class)
public class ViewStructuresDisplayContextTest
	extends BaseDisplayContextTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Test
	public void testGetCreationMenu() throws Exception {
		CreationMenu creationMenu = ReflectionTestUtil.invoke(
			_getSectionDisplayContext(getMockHttpServletRequest()),
			"getCreationMenu", new Class<?>[0]);

		List<DropdownItem> dropdownItems = (List<DropdownItem>)creationMenu.get(
			"primaryItems");

		Map<String, String> expectedCreationMenuItems =
			LinkedHashMapBuilder.put(
				"content",
				GroupConstants.CMS_FRIENDLY_URL +
					"/structure-builder?objectFolderExternalReferenceCode=" +
						"L_CMS_CONTENT_STRUCTURES"
			).put(
				"file",
				GroupConstants.CMS_FRIENDLY_URL +
					"/structure-builder?objectFolderExternalReferenceCode=" +
						"L_CMS_FILE_TYPES"
			).build();

		Assert.assertEquals(
			dropdownItems.toString(), expectedCreationMenuItems.size(),
			dropdownItems.size());

		int index = 0;

		for (Map.Entry<String, String> entry :
				expectedCreationMenuItems.entrySet()) {

			DropdownItem dropdownItem = dropdownItems.get(index);

			Assert.assertEquals(entry.getKey(), dropdownItem.get("label"));
			Assert.assertEquals(entry.getValue(), dropdownItem.get("href"));

			index++;
		}
	}

	@Test
	public void testGetFDSActionDropdownItems() throws Exception {
		List<FDSActionDropdownItem> fdsActionDropdownItems =
			ReflectionTestUtil.invoke(
				_getSectionDisplayContext(getMockHttpServletRequest()),
				"getFDSActionDropdownItems", new Class<?>[0]);

		Assert.assertEquals(
			fdsActionDropdownItems.toString(), 7,
			fdsActionDropdownItems.size());

		_assertFDSActionDropdownItem(
			fdsActionDropdownItems.get(0), "pencil", "edit", "edit", "get",
			Map.of("system", false));
		_assertFDSActionDropdownItem(
			fdsActionDropdownItems.get(1), "list-ul", "viewUsages",
			"view-usages", "get", null);
		_assertFDSActionDropdownItem(
			fdsActionDropdownItems.get(2), "copy", "copy", "make-a-copy", null,
			null);
		_assertFDSActionDropdownItem(
			fdsActionDropdownItems.get(3), "export", "export", "export-as-json",
			"get", Map.of("system", false));
		_assertFDSActionDropdownItem(
			fdsActionDropdownItems.get(4), "import", "import",
			"import-and-override", "get", Map.of("system", false));
		_assertFDSActionDropdownItem(
			fdsActionDropdownItems.get(5), "password-policies", "permissions",
			"permissions", "get", null);
		_assertFDSActionDropdownItem(
			fdsActionDropdownItems.get(6), "trash", "delete", "delete",
			"delete", Map.of("system", false));
	}

	private void _assertFDSActionDropdownItem(
		FDSActionDropdownItem fdsActionDropdownItem, String icon, String id,
		String label, String method, Map<String, Object> visibilityFilters) {

		Assert.assertNotNull(fdsActionDropdownItem);

		Map<String, Object> data =
			(Map<String, Object>)fdsActionDropdownItem.get("data");

		Assert.assertEquals(id, data.get("id"));
		Assert.assertEquals(method, data.get("method"));

		Assert.assertEquals(icon, fdsActionDropdownItem.get("icon"));
		Assert.assertEquals(label, fdsActionDropdownItem.get("label"));

		if (visibilityFilters != null) {
			Assert.assertEquals(
				data.get("visibilityFilters"), visibilityFilters);
		}
	}

	private Object _getSectionDisplayContext(
			HttpServletRequest httpServletRequest)
		throws Exception {

		_fragmentRenderer.render(
			null, httpServletRequest, new MockHttpServletResponse());

		Object viewStructuresDisplayContext = httpServletRequest.getAttribute(
			"com.liferay.site.cms.site.initializer.internal.display.context." +
				"ViewStructuresDisplayContext");

		Assert.assertNotNull(viewStructuresDisplayContext);

		return viewStructuresDisplayContext;
	}

	@Inject(
		filter = "component.name=com.liferay.site.cms.site.initializer.internal.fragment.renderer.ViewStructuresJSPSectionFragmentRenderer"
	)
	private FragmentRenderer _fragmentRenderer;

}