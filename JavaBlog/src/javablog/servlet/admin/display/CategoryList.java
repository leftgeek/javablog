package javablog.servlet.admin.display;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;

import javablog.database.DBConnectionPool;
import javablog.util.ConfigProperty;
import javablog.util.StrFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javablog.bean.*;

public class CategoryList extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public CategoryList() {
		super();
	}

	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		boolean flag = false;
		HttpSession session = request.getSession(true);
		String userIdText = (String) session.getAttribute("userId");
		if (userIdText != null) {
			String statusText = request.getParameter("status");// 获得文章类型
			String attribute = request.getParameter("attribute");// 排序字段
			String order = request.getParameter("order");// 排序方式
			String pageNowText = request.getParameter("pageNow");// 获得要显示的页数
			
			StrFilter sf = new StrFilter();
			int status = sf.parseInt(statusText);
			int pageNow = sf.parseInt(pageNowText);
			if (pageNow == 0) {
				pageNow = 1;
			}
			if (attribute == null){
				attribute = "";
			}
			if (order == null){
				order = "";
			}

			if (this.isValidStatus(status)
					&& this.isValidAttribute(attribute)
					&& sf.isValidOrder(order)
					&& pageNow > 0){
				Connection connection = DBConnectionPool.getDBConnectionPool().getConnection();
				if (connection != null){			
					UserBeanBo ubb = new UserBeanBo(connection);
					UserBean ub = ubb.getUser(Integer.parseInt(userIdText));
					if (ub != null && ub.getStatus() == ConfigProperty.STATUS_NORMAL){
						RoleBeanBo rbb = new RoleBeanBo(connection);
						RoleBean role = rbb.getRole(ub.getRoleId());
						if (role != null && role.getStatus() == 0 && role.canReadCategory()) {
							CategoryBeanBo cbb = new CategoryBeanBo(connection);
							cbb.setPageSize(ConfigProperty.admin_category_page_size);
							cbb.setFilter(status, attribute, order);
							ArrayList<CategoryBean> al = cbb.getCategories(pageNow);
							request.setAttribute("categoryList", al);
							request.setAttribute("role", role);
							request.setAttribute("pageNow", pageNow + "");
							request.setAttribute("rowCount", cbb.getRowCount() + "");
							request.setAttribute("pageCount", cbb.getPageCount() + "");
							request.setAttribute("attribute", attribute);
							request.setAttribute("order", order);
							request.setAttribute("status", status + "");
							
							flag = true;
						}
					}
					ubb.closeConnection();
				}
			}
		}
		
		if (flag){
			request.getRequestDispatcher("categoryList.jsp").forward(request,
					response);
		} else {
			request.getRequestDispatcher("error.jsp")
					.forward(request, response);
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

	public void init() throws ServletException {
		// Put your code here
	}

	private boolean isValidAttribute(String attribute){
		boolean result = false;
		if (attribute == null
				|| attribute.length() == 0
				|| attribute.equalsIgnoreCase("category_id")
				|| attribute.equalsIgnoreCase("name")){
			result = true;
		}
		return result;
	}

	private boolean isValidStatus(int status){
		boolean result = false;
		if (status == ConfigProperty.STATUS_NORMAL
				|| status == ConfigProperty.STATUS_TRASH){
			result = true;
		}
		return result;
	}
}
