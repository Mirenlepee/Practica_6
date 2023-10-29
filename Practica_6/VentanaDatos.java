package Practica_6;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.*;
import javax.swing.tree.*;


public class VentanaDatos  extends JFrame {
	//Atributos

	private MiJTree treeMunis;
	private JTable tablaMunis;
    private DataSetMunicipios datosMunis;
	private TableModel modeloDatos;
	private DefaultTreeModel modeloArbol;
	private DefaultMutableTreeNode raiz;
	private int criterioOrden;  //0 si esta en orden alfabético y 1 si esta ordenado por habitantes
	private JPanel graficoMunis;
	
	public VentanaDatos( JFrame ventOrigen ) {
		// Configuración de la ventana
		setTitle("Ventana de Datos");
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setSize( 800, 600 );
		setLocationRelativeTo( null );
		setLayout(new BorderLayout());
		
		
		// Label en la parte superior
	    JLabel mensajeLbl = new JLabel(" ");
	    add(mensajeLbl, BorderLayout.NORTH);
	    
	    // TREE
	    treeMunis = new MiJTree();
	    add(new JScrollPane( treeMunis ), BorderLayout.WEST);  
	    
	    // TABLA
	    tablaMunis = new JTable();
	   	add( new JScrollPane( tablaMunis ), BorderLayout.CENTER );
        
        // PANEL DE VISUALIZACION - GRAFICO  
        graficoMunis = new JPanel() {
        	@Override
        	       protected void paintComponent(Graphics g) {
        	           super.paintComponent(g);
        	           dibujarGrafico((Graphics2D) g);
        	           {
        	               setOpaque(true);
        	               setPreferredSize(new Dimension(300, 600));
        	           }
        	       }
        	};
        this.add(graficoMunis, BorderLayout.EAST);
       
        // BOTONERA
        JPanel botonera = new JPanel();
        JButton btnInsercion = new JButton("Inserción");
        JButton btnBorrado = new JButton("Borrado");
        JButton btnOrden = new JButton("Orden");
        botonera.add(btnInsercion);
        botonera.add(btnBorrado);
        botonera.add(btnOrden);
        add(botonera, BorderLayout.SOUTH);
              
                
        btnInsercion.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
	        	int filaSel = tablaMunis.getSelectedRow();
	        	if(filaSel >= 0) {
	        		String provinciaSel = (String) tablaMunis.getValueAt(filaSel, 4);
	        	    String autonomiaSel = (String) tablaMunis.getValueAt(filaSel, 5);
	        	    Municipio nuevo = new Municipio(datosMunis.getListaMunicipios().size()+1, " ", 50000, provinciaSel, autonomiaSel);
	        	    datosMunis.anyadir(nuevo);
	        	    int filaNueva = modeloDatos.getRowCount()+1;
	        	    ((MiTableModel) modeloDatos).anyadirFila(filaNueva);
	        	    ((MiTableModel) modeloDatos).setListaMunicipios(datosMunis.getMunicipiosEnProvincia(provinciaSel));
	        	    tablaMunis.repaint();
	        	}
        	}
        });       
        
        btnBorrado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	int filaSel = tablaMunis.getSelectedRow();
            	if(filaSel >= 0) {
            	String provinciaSel = (String) tablaMunis.getValueAt(filaSel, 4);
            	int opcion = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que quieres borrar este municipio?", "Confirmación de Borrado", JOptionPane.YES_NO_OPTION);
            	           if (opcion == JOptionPane.YES_OPTION) {
            	               ((MiTableModel) modeloDatos).borrarFila(filaSel);
            	               ((MiTableModel) modeloDatos).setListaMunicipios(datosMunis.getMunicipiosEnProvincia(provinciaSel));
            	               tablaMunis.setModel(modeloDatos);
            	               tablaMunis.repaint();
            	           }
            	       } else {
            	           JOptionPane.showMessageDialog(null, "Selecciona un municipio para borrar.", "Error", JOptionPane.ERROR_MESSAGE);
            	       }
            	}
        });               

        
        btnOrden.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	String provincia = treeMunis.getSelectionPath().getLastPathComponent().toString();
            	//System.out.println(provincia);
            	List<Municipio> municipiosEnProvincia = datosMunis.getMunicipiosEnProvincia(provincia);

            	if(criterioOrden == 1) {
            	municipiosEnProvincia.sort(Comparator.comparing(Municipio::getNombre));
            	criterioOrden = 0;

            	}else {
            	municipiosEnProvincia.sort(Comparator.comparingInt(Municipio::getHabitantes).reversed());
            	criterioOrden = 1;
            	}
            	((MiTableModel) tablaMunis.getModel()).setListaMunicipios(municipiosEnProvincia);
            	tablaMunis.repaint();
            }
        });
        
	}
	
	private String obtenerProvinciaSeleccionada() {
	    TreePath path = treeMunis.getSelectionPath();
	    if (path != null) {
	        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
	        Object userObject = selectedNode.getUserObject();
	        if (userObject instanceof String) {
	            // Asumiendo que el nombre de la provincia está almacenado en el userObject
	            return (String) userObject;
	        }
	    }
	    return null;
	}

	// TREE
	public void setTree(DataSetMunicipios datosMunis) {
		this.datosMunis = datosMunis;
	    raiz = new DefaultMutableTreeNode("Municipios");
	    modeloArbol = new DefaultTreeModel(raiz);
	    treeMunis.setModel(modeloArbol);
	    treeMunis.setEditable(false);
	    rendererProvincia(treeMunis, datosMunis);
	    Map<String, DefaultMutableTreeNode> comunidades = new HashMap<>();
        Set<String> provinciasAgregadas = new HashSet<>();

        
	    if (datosMunis != null && datosMunis.getListaMunicipios() != null) {
	       

	        for (Municipio municipio : datosMunis.getListaMunicipios()) {
	            String comunidadAutonoma = municipio.getAutonomia();
	            DefaultMutableTreeNode comunidadNode = comunidades.get(comunidadAutonoma);

	            if (comunidadNode == null) {
	                comunidadNode = new DefaultMutableTreeNode(comunidadAutonoma);
//	                raiz.add(comunidadNode);
	                modeloArbol.insertNodeInto(comunidadNode, raiz, raiz.getChildCount());
	                comunidades.put(comunidadAutonoma, comunidadNode);
	            }
	            String provincia = municipio.getProvincia();

	            if (!provinciasAgregadas.contains(provincia)) {
	                DefaultMutableTreeNode provinciaNode = new DefaultMutableTreeNode(provincia);
//	                comunidadNode.add(provinciaNode);
//	                modeloArbol.insertNodeInto( provinciaNode, comunidadNode, 0 );
	                modeloArbol.insertNodeInto(provinciaNode, comunidadNode, comunidadNode.getChildCount());
	                provinciasAgregadas.add(provincia);
	            }
	        }
	        treeMunis.expandir(new TreePath(raiz.getPath()), true);	
	    }
	    
	    treeMunis.addTreeSelectionListener(new TreeSelectionListener() {
	        @Override
	        public void valueChanged(TreeSelectionEvent e) {
	            TreePath tp = e.getPath();
	            if (tp != null) {
	                DefaultMutableTreeNode nodeSel = (DefaultMutableTreeNode) tp.getLastPathComponent();
	                if (nodeSel != null && nodeSel.isLeaf()) {
	                    String provinciaSel = (String) nodeSel.getUserObject();
	                    CargarDatosTabla(provinciaSel);
	                    rendererProvincia(treeMunis, datosMunis);
	                    graficoMunis.repaint();
	                }
	            }
	        }
	    });
	}
	
	private void rendererProvincia(JTree treeDatos, DataSetMunicipios datosMunis) {
		   treeDatos.setCellRenderer(new DefaultTreeCellRenderer() {
		       @Override
		       public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
		                                                     boolean leaf, int row, boolean hasFocus) {
		           Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		           if (value instanceof DefaultMutableTreeNode) {
		               DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
		               if (datosMunis.getListaProvincias().contains(node.getUserObject())) {
		           
		                   String provincia = (String) node.getUserObject();
		                   
		                   int habitantes = 0;
		                   for(int i=0; i<datosMunis.getMunicipiosEnProvincia(provincia).size(); i++) {
		                    habitantes += datosMunis.getMunicipiosEnProvincia(provincia).get(i).getHabitantes();
		                   }
		                   JProgressBar progressBar = new JProgressBar();
		                   progressBar.setMaximum(5000000);
		                   progressBar.setValue(habitantes);

		                   JPanel panel = new JPanel(new BorderLayout());
		                   panel.add(new JLabel(provincia), BorderLayout.WEST);
		                   panel.add(progressBar, BorderLayout.EAST);

		                   return panel;
		               }
		           }
		           return c;
		       }
		   });
		}

	public static class MiJTree extends JTree {
		public void expandir( TreePath path, boolean estado ) {
			setExpandedState( path, estado );
		}
	}
	
	//TABLA

	private void CargarDatosTabla(String provinciaSel) {
		List<Municipio> municipiosEnProvincia = datosMunis.getMunicipiosEnProvincia(provinciaSel);
		municipiosEnProvincia.sort(Comparator.comparing(Municipio::getNombre));
		modeloDatos = new MiTableModel(municipiosEnProvincia);
		tablaMunis.setModel(modeloDatos);

		tablaMunis.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer() {
			private JProgressBar pbPoblacion = new JProgressBar(50000, 5000000);
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if(column == 3) {
					int poblacion = (Integer) value;
					double porcentaje = (double) (poblacion - 50000) / (5000000 - 50000);

					int red = (int) (255 * porcentaje);
					int green = (int) (255 * (1 - porcentaje));

					pbPoblacion.setValue(poblacion);
					pbPoblacion.setForeground(new Color(red, green, 0));

					return pbPoblacion;
				}else {
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}
		});

		tablaMunis.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				boolean municipioIsSel;
				int filaEnTabla = tablaMunis.rowAtPoint(e.getPoint());
				int colEnTabla = tablaMunis.columnAtPoint(e.getPoint());
				if(colEnTabla == 1 && filaEnTabla >= 0) {
					Municipio municipioSel = municipiosEnProvincia.get(filaEnTabla);
					municipioIsSel = true;
					colorearCelda(municipioSel, municipioIsSel, municipiosEnProvincia);
				}else {
					municipioIsSel = false;
					colorearCelda(null, municipioIsSel, null);
				}
			}
		});
	}	
	private void colorearCelda(Municipio municipioSel, boolean municipioIsSel, List<Municipio> municipiosEnProvincia) {
	    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			@Override
	        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

	            if (municipioIsSel && column == 1) {
	                Municipio municipioActual = municipiosEnProvincia.get(row);

	                if (municipioActual.equals(municipioSel)) {
	                    c.setBackground(Color.WHITE);
	                } else if (municipioActual.getHabitantes() > municipioSel.getHabitantes()) {
	                    c.setBackground(Color.RED);
	                } else if (municipioActual.getHabitantes() < municipioSel.getHabitantes()) {
	                    c.setBackground(Color.GREEN);
	                } else {
	                    c.setBackground(Color.WHITE);
	                }
	            } else {
	                c.setBackground(table.getBackground());
	            }
	            return c;
	        }
	    };

	    tablaMunis.getColumnModel().getColumn(1).setCellRenderer(renderer); // Asegura que el renderer se aplique solo a la columna de nombres (ajusta el índice de la columna según sea necesario)
	    tablaMunis.repaint();
	}
	
	// Define una clase MiTableModel que implementa TableModel
	private class MiTableModel implements TableModel {

		private final Class<?>[] CLASES_COLS = { Integer.class, String.class, Integer.class, Integer.class, String.class, String.class };
		private List<Municipio> listaMunicipios;

		private MiTableModel(List<Municipio> municipios) {
			super();
			this.listaMunicipios = municipios;
		}

		public void setListaMunicipios(List<Municipio> municipiosEnProvincia) {
			this.listaMunicipios = municipiosEnProvincia;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return CLASES_COLS[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public int getRowCount() {
			return listaMunicipios.size();
		}

		private static final String[] cabeceras = {"Código", "Nombre", "Habitantes", "Población", "Provincia", "Autonomia"};
		@Override
		public String getColumnName(int columnIndex) {
			return cabeceras[columnIndex];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Municipio m = listaMunicipios.get(rowIndex);
			switch(columnIndex) {
			case 0:
				return m.getCodigo();
			case 1:
				return m.getNombre();
			case 2:
				return m.getHabitantes();
			case 3:
				return m.getHabitantes();
			case 4:
				return m.getProvincia();
			case 5:
				return m.getAutonomia();
			default:
				return null;
			}
		}
		
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if(columnIndex == 1 || columnIndex == 2) {
				return true;
			}
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

			Municipio m = listaMunicipios.get(rowIndex);
			switch(columnIndex) {
			case 0:
				m.setCodigo((Integer) aValue);
				break;
			case 1:
				m.setNombre((String) aValue);
				break;
			case 2:
				try {
					m.setHabitantes((Integer) aValue);
					tablaMunis.repaint();
				}catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Numero de habitantes erróneo");
				}
				break;
			case 3:
				try {
					m.setHabitantes((Integer) aValue);
				}catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Numero de habitantes erróneo");
				}
				break;
			case 4:
				m.setProvincia((String) aValue);
				break;
			case 5:
				m.setAutonomia((String) aValue);
				break;
			}
		}

		ArrayList<TableModelListener> listaEsc = new ArrayList<>();
		@Override
		public void addTableModelListener(TableModelListener l) {
			listaEsc.add(l);
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
			listaEsc.remove(l);
		}

		
		public void fireTableChanged(TableModelEvent e) {
			for(TableModelListener l: listaEsc) {
				l.tableChanged(e);
			}
		}
		
		public void borrarFila(int fila) {
			if (fila >= 0 && fila < listaMunicipios.size()) {
			       Municipio municipioBorrado = listaMunicipios.remove(fila);
			       datosMunis.quitar(municipioBorrado.getCodigo());
			       fireTableChanged(new TableModelEvent(modeloDatos, fila, datosMunis.getListaMunicipios().size()));
			}
		}
		
		public void anyadirFila(int fila) {
			fireTableChanged(new TableModelEvent(modeloDatos, fila, datosMunis.getListaMunicipios().size()));
			   tablaMunis.repaint();
		}   
	}
	
	//PANEL

	private void dibujarGrafico(Graphics2D grafico) {
	        int anchoPanelGraf = graficoMunis.getWidth();
	        int altoPanelGraf = graficoMunis.getHeight();

	        if(treeMunis.getSelectionPath() != null) {
	            DefaultMutableTreeNode nodoSel = (DefaultMutableTreeNode) treeMunis.getSelectionPath().getLastPathComponent();
	            String provinciaSel = "";
	            if (nodoSel != null && nodoSel.isLeaf()) {
	            provinciaSel = (String) nodoSel.getUserObject();
	            }
	       List<Municipio> municipiosEnProvincia = datosMunis.getMunicipiosEnProvincia(provinciaSel);
	       int poblacionProv= 0;
	       int poblacionTotal = 0;

	       for (Municipio municipio : municipiosEnProvincia) {
	           poblacionProv += municipio.getHabitantes();
	       }

	       for (Municipio municipio : datosMunis.getListaMunicipios()) {
	           poblacionTotal += municipio.getHabitantes();
	       }

	       int anchoProv = (anchoPanelGraf / 2) - 20;
	       int alturaMax = altoPanelGraf - 40;
	       double porcentajeProv = (double) poblacionProv / poblacionTotal;
	       int alturaProv = (int) (porcentajeProv * alturaMax);
	       int xBarraProv = 10;
	       int yBarraProv = altoPanelGraf - alturaProv;

	       grafico.setColor(Color.GREEN);
	       grafico.fillRect(xBarraProv, yBarraProv, anchoProv, alturaProv);

	       grafico.setColor(Color.BLACK);
	       int ySeparador = yBarraProv;
	       for (Municipio municipio : municipiosEnProvincia) {
	           int alturaSeparador = (int) ((double) municipio.getHabitantes() / poblacionProv * alturaProv);
	           grafico.drawLine(xBarraProv, ySeparador, xBarraProv + anchoProv, ySeparador);
	           grafico.drawLine(xBarraProv, ySeparador + alturaSeparador, xBarraProv + anchoProv, ySeparador + alturaSeparador);
	           ySeparador += alturaSeparador;
	       }

	       int anchoEstado = (anchoPanelGraf / 2) - 20;
	       int xBarraEstado = xBarraProv + anchoProv + 10;
	       int alturaEstado = alturaMax;
	       int yBarraEstado = altoPanelGraf - alturaEstado;

	       grafico.setColor(Color.BLUE);
	       grafico.fillRect(xBarraEstado, yBarraEstado, anchoEstado, alturaEstado);

	       grafico.setColor(Color.BLACK);    
	        }
	    }

}
