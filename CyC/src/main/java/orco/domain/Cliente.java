package orco.domain;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import orco.filter.ClienteFilter;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.entity.RooEntity;

@RooJavaBean
//@RooToString
@RooEntity(finders = { "findClientesByUserNamePortal" })
public class Cliente implements Comparable<Cliente> {

    @NotNull
    @Size(min = 2, max = 255)
    private String nombre;

    @Size(max = 255)
    private String direccion;

    @Size(max = 255)
    private String ciudad;

    @Size(max = 50)
    private String provincia;

    @Size(max = 25)
    private String cuit;

    @Size(max = 50)
    private String telefono;

    @Size(max = 30)
    private String userNamePortal;

    @Size(max = 30)
    private String passwordPortal;

    @Override
    public int compareTo(Cliente o) {
        if (this.getNombre() != null) {
            return this.getNombre().compareToIgnoreCase(o.getNombre());
        } else {
            return 1;
        }
    }
    
    
    public static List<Cliente> findAllClientes() {
        return entityManager().createQuery("select o from Cliente o order by o.nombre", Cliente.class).getResultList();
    }
    
    public static Cliente findCliente(Long id) {
        if (id == null) return null;
        return entityManager().find(Cliente.class, id);
    }
    
    public static List<Cliente> findClienteEntries(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Cliente o order by o.nombre", Cliente.class).setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    

    
    /**
     * Devuelve la cantidad de Facturas que corresponden con el filtro
     * @param ordenTrabajoFilter
     * @return
     */
    @SuppressWarnings("unchecked")
	public static long countFindCliente(ClienteFilter facturaFilter) {
        TypedQuery<Long> query = (TypedQuery<Long>) queryFindCliente(facturaFilter, true);
        return query.getSingleResult();
    }
    
    
    /**
     * Devuelve todas las Facturas que correspondan con el filtro
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<Cliente> findCliente(ClienteFilter clienteFilter, int firstResult, int maxResults) {
        TypedQuery<Cliente> query = (TypedQuery<Cliente>) queryFindCliente(clienteFilter, false);
        return query.setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

    /**
     * Devuelve una query de acuerdo al filtro y si quere la cantidad o el listado mismo
     * @param facturaFilter
     * @param soloContarCantidad discrimina si la query es de cantidad o de listado de objetos
     * @return
     */
	private static Query queryFindCliente(ClienteFilter clienteFilter, boolean soloContarCantidad) {
		StringBuilder querySB = new StringBuilder();
        StringBuilder whereSB = new StringBuilder();
        
        if (soloContarCantidad){
        	querySB.append("select count(o) from Cliente o");
        } else {
        	querySB.append("select o from Cliente o");
        }
       
        if (clienteFilter != null) {
            if (clienteFilter.getTexto() != null && clienteFilter.getTexto().trim().length() > 0) {
                whereSB.append(" and (o.nombre like :texto " + "or" + " o.cuit like :texto) ");
                	                
            }
        }
        if (whereSB.toString().trim().length() > 0) {
            querySB.append(" where ").append(whereSB.toString().substring(4));
        }
        Query query = null;
        if (soloContarCantidad){
          	query = entityManager().createQuery(querySB.toString(), Long.class);
        } else {
        	query = entityManager().createQuery(querySB.toString(), Cliente.class);
        }
        
        if (clienteFilter != null) {
            if (clienteFilter.getTexto() != null && clienteFilter.getTexto().trim().length() > 0) {
            	query.setParameter("texto", "%" + clienteFilter.getTexto() + "%");
                    	                
            }
        }
       
		return query;
	}
    
    public String toString() {
        return nombre;
    }
}
