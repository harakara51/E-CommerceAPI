package data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class MCDAO
{
	@PersistenceContext
	EntityManager em;

	public productsEntitie getSingleProduct(int id)
	{
		productsEntitie product = (productsEntitie) em.createNamedQuery("getProductsById").setParameter("id", id)
				.getSingleResult();
		return product;
	}

	public List<productsEntitie> getAllProducts()
	{
		List<productsEntitie> products = (List<productsEntitie>) em.createNamedQuery("getAllProducts").getResultList();
		return products;
	}

	public List<productsEntitie> getCategories(String cat)
	{
		System.out.println("in dao get categories " + cat);
		List<productsEntitie> products = (List<productsEntitie>) em.createNamedQuery("getProductsBycatagory")
				.setParameter("cat", cat).getResultList();
		return products;
	}
	
	public addressEntitie getAddress(String id){
		try{
		addressEntitie aE = (addressEntitie) em.createNamedQuery("getAddressbyUser")
		.setParameter("id", id).getSingleResult();
		return aE;
		}
		catch(Exception e){
			return null;
		}
	}
	public List<productsEntitie> searchProducts(String id)
	{
		System.out.println("in dao get products " + id);
		List<productsEntitie> products = em.createQuery("select u from productsEntitie u where u.name like :searchID or u.brand like :searchID or u.catagory like :searchID", productsEntitie.class).setParameter("searchID", "%"+id+"%").getResultList();
		System.out.println("results: "+products);
		return products;
	}



public List<productsEntitie> getShoppingCartItems (String id) 
{
	System.out.println("in dao for get shoppingcartItems and id is " + id);
	userEntitie userID = (userEntitie)em.createNamedQuery("getUserById").setParameter("id", Integer.parseInt(id)).getSingleResult();
	int ShoppingCartID = userID.getCart().getId();
	System.out.println(ShoppingCartID);
//	System.out.println("in dao get categories "+id);
 List<productsEntitie> products = (List<productsEntitie>)em.createNamedQuery("getSCitemsbyID").setParameter("id", ShoppingCartID).getResultList();
 return products;
}

public void addToCart(String json){
	int productID = Integer.parseInt(json.split(":")[1].split(",")[0].replaceAll("\"", ""));
	int userID = Integer.parseInt(json.split(":")[2].replace("}", ""));
	System.out.println(productID);
	userEntitie user = em.find(userEntitie.class, userID);
	productsEntitie product = em.find(productsEntitie.class, productID);
	System.out.println(product);
	ShoppingCartItemsEntitie scie = null;
	System.out.println(user.getCart() + " user.getCart()");
		if (user.getCart() != null){
			System.out.println("inside cart is already there add to cart");
			user.getCart().addToProductsList(product);
			try{
				System.out.println("in try block of add to cart dao method");
				scie = (ShoppingCartItemsEntitie) em.createNamedQuery("getShoppingCartItem").setParameter("sid", user.getCart()).setParameter("pid", product).getSingleResult();
				System.out.println("1");
				scie.setQuantity(scie.getQuantity() + 1);
				System.out.println("2");
				System.out.println(scie);
				System.out.println("3");
			}
			catch(Exception e){
				System.out.println("in catch block of add to cart dao method");
				scie = new ShoppingCartItemsEntitie();
				scie.setShoppingCart_id(user.getCart());
				scie.setProducts_id(product);
				scie.setQuantity(1);
			}
		}
		else {
			System.out.println("inside else statement for add to cart");
			shoppingCartEntitie mycart = new shoppingCartEntitie();
			user.setCart(mycart);
			System.out.println("inside else statement after user.setCart(mycart)");
			scie = new ShoppingCartItemsEntitie();
			System.out.println("inside else statement after scie = new ShoppingCartItemsEntitie()");
			scie.setProducts_id(product);
			scie.setShoppingCart_id(user.getCart());
			scie.setQuantity(1);
			
			user.getCart().setUsers_id(user);
			System.out.println("inside else statement after user.getCart().setUsers_id(user)");
			user.getCart().setType("shopping cart");
			user.getCart().addToProductsList(product);
			
			//em.merge(mycart);
			System.out.println("inside else statement after em.merge(mycart)");
		}
	
	em.merge(user);
	em.merge(product);
	em.persist(scie);
//	System.out.println(user.getCart().getProductsList());
//	System.out.println(user.getCart().getProductsList().size());
	}
	public List<productsEntitie> displayCartItems(String id)
	{
		System.out.println("in dao");
		System.out.println("id: "+id);
//		List<productsEntitie> products = em.createQuery("select p from productsEntitie p join userEntitie ue where ue.id = shoppingCartEntitie sce ShoppingCartItemsEntitie scie where p.id = scie.products_id", productsEntitie.class).setParameter("searchID", "%"+id+"%").getResultList();
		shoppingCartEntitie shoppingCart = em.createQuery("select sce from shoppingCartEntitie sce where sce.user_id = :searchID", shoppingCartEntitie.class).setParameter("searchID", id).getSingleResult();
		System.out.println("1");
		System.out.println(shoppingCart.getId());
		List<ShoppingCartItemsEntitie> shoppingCartItems = em.createQuery("select scie from ShoppingCartItemsEntitie scie where scie.shoppingCart_id = :searchID",ShoppingCartItemsEntitie.class).setParameter("searchID", "%"+shoppingCart.getId()+"%").getResultList();
		System.out.println("2");
		List<productsEntitie> products = new ArrayList<>(); 
		for (ShoppingCartItemsEntitie scie : shoppingCartItems)
		{
			products.add(em.createQuery("select p from productsEntitie p where p.id = :searchID", productsEntitie.class).setParameter("searchID", "%"+scie.getProducts_id()+"%").getSingleResult());
		}
		System.out.println("3");
		return products;
	}

	// public productsEntitie getProductsbyID (String json){
	//
	//
	//
	//
	// ObjectMapper mapper = new ObjectMapper();
	// productsEntitie product;
	// try
	// {
	// prod = mapper.readValue(json, userEntitie.class);
	// String email = user.getEmail();
	// String password = user.getPassword();
	//
	// userEntitie checkUser = getUserByEmail(email);
	// if (checkUser.getEmail().equals(email) &&
	// checkUser.getPassword().equals(password))
	// {
	// return user;
	// }
	// }
	// catch (IOException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	//
	//
	//
	//
	//
	//
	// System.out.println("inside DAO Id");
	// productsEntitie product =
	// (productsEntitie)em.createNamedQuery("getProductsById").setParameter("id",
	// id).getSingleResult();
	// return product;
	// }

}