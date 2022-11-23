package cs107KNN;
import java.util.ArrayList;
public class KNN {
    
	public static void main(String[] args) {
		byte b1 = 40; // 00101000
		byte b2 = 20; // 00010100
		byte b3 = 10; // 00001010
		byte b4 = 5; // 00000101

		// [00101000 | 00010100 | 00001010 | 00000101] = 672401925
		int result = extractInt(b1, b2, b3, b4);
		System.out.println(result);

		String bits = "10000001";
		System.out.println("La séquence de bits " + bits + "\n\tinterprétée comme byte non signé donne "
				+ Helpers.interpretUnsigned(bits) + "\n\tinterpretée comme byte signé donne "
				+ Helpers.interpretSigned(bits));

  /*    // Charge les étiquettes depuis le disque 
byte[] labelsRaw =Helpers.readBinaryFile("datasets/10-per-digit_labels_train");
// Parse les étiquettes
byte[] labelsTrain = parseIDXlabels( labelsRaw);
// Affiche le nombre de labels
System.out.println(labelsTrain.length);
// Affiche le premier label
System.out.println(labelsTrain[0]);
// Charge les images depuis le disque 
byte[] imagesRaw =Helpers.readBinaryFile("datasets/10-per-digit_images_train");
// Parse les images
byte[][][] imagesTrain = parseIDXimages( imagesRaw);
// Affiche les dimensions des images
System.out.println("Number of images : " + imagesTrain.length);
System.out.println("height : " + imagesTrain[0].length);
System.out.println("width : " + imagesTrain[0][0].length);
// Affiche les 30 premières images et leurs étiquettes 
Helpers.show("Test", imagesTrain, labelsTrain, 2, 15);
*/
int TESTS = 1500 ;
int K = 5 ;
byte[][][] trainImages =parseIDXimages( Helpers. readBinaryFile("datasets/10-per-digit_images_train")) ; 
byte[] trainLabels =parseIDXlabels(Helpers.readBinaryFile("datasets/10-per-digit_labels_train"));
byte[][][] testImages =parseIDXimages( Helpers. readBinaryFile("datasets/10k_images_test")) ; 
byte[] testLabels =parseIDXlabels(Helpers.readBinaryFile("datasets/10k_labels_test"));
byte[] predictions = new byte[ TESTS]; 
for ( int i = 0 ; i < TESTS ; i++)
 {
    predictions[i] = knnClassify(testImages[i], trainImages, trainLabels, K);
 }
Helpers.show("Test", testImages, predictions, testLabels, 20,35);






	}

	/**
	 * Composes four bytes into an integer using big endian convention.
	 *
	 * @param bXToBY The byte containing the bits to store between positions X and Y
	 * 
	 * @return the integer having form [ b31ToB24 | b23ToB16 | b15ToB8 | b7ToB0 ]
	 */
	public static int extractInt(byte b31ToB24, byte b23ToB16, byte b15ToB8, byte b7ToB0) {
		
        byte[] tab=new byte[]{ b31ToB24 , b23ToB16, b15ToB8,b7ToB0} ;
        return ((tab[0] & 0xFF) << 24)|
                ((tab[1] & 0xFF) << 16)|
                ((tab[2] & 0xFF) << 8)|
                ((tab[3] & 0xFF) << 0);
    }

	/**
	 * Parses an IDX file containing images
	 *
	 * @param data the binary content of the file
	 *
	 * @return A tensor of images
	 */
	public static byte[][][] parseIDXimages(byte[] data) {
       int mag=extractInt(data[0],data[1],data[2],data[3]);
       if (mag!=2051)
              {
                  System.out.println("il y une erreur : le nombre magique est incorrect");
                  return(null);
              }
      int nbr=extractInt(data[4],data[5],data[6],data[7]);
      int h=extractInt(data[8],data[9],data[10],data[11]);
      int l=extractInt(data[12],data[13],data[14],data[15]);
      byte [][][] tabi=new byte[nbr][h][l];
      int t=h*l;
      int v=16;
      for(int i=0;i<nbr;i++)
      {
          tabi[i]=image1(h,l,data,v);
          v+=t;
      }
      return(tabi);
	}
    public static byte [][] image1(int h ,int l , byte [] data,int v)
    {   byte [][] tab=new byte[h][l];
        for(int j=0;j<h;j++)
          {
            for(int k=0;k<l;k++)
          {  
              tab[j][k]=(byte)((data[v] & 0xff)-128);
              v+=1;
              
          }  
          }
          return(tab);
    }

	/**
	 * Parses an idx images containing labels
	 *
	 * @param data the binary content of the file
	 *
	 * @return the parsed labels
	 */
	public static byte[] parseIDXlabels(byte[] data) {
		
              int mag=extractInt(data[0],data[1],data[2],data[3]);
              if (mag!=2049)
              {
                  System.out.println("il y une erreur : le nombre magique est incorrect");
                  return(null);
              }
              int nbr=extractInt(data[4],data[5],data[6],data[7]);
              if (nbr!=data.length-8)
              {
                 System.out.println("il y une erreur : le nombre d'etiquettes n'est pas correct ");
                 return(null);}
              byte [] tabe=new byte[nbr];
              for (int i=0;i<nbr;i++)   
                tabe[i]=data[8+i];
             return(tabe) ;
	}

	/**
	 * @brief Computes the squared L2 distance of two images
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the squared euclidean distance between the two images
	 */
	public static float squaredEuclideanDistance(byte[][] a, byte[][] b) {
                double x = 0;
                float e;
                if ((a.length!=b.length)||(a[0].length!=b[0].length))
                {
                    System.out.println("il y a une erreur de parametres");
                    return 0f ;
                }
                for (int i=0;i<a.length;i++)
                {
                    for (int j=0;j<a[0].length;j++)
                     x+=Math.pow((a[i][j]-b[i][j]),2);          
                }
                e=(float)x;
		return e;
	}

	/**
	 * @brief Computes the inverted similarity between 2 images.
	 * 
	 * @param a, b two images of same dimensions
	 * 
	 * @return the inverted similarity between the two images*/
	 
	public static float invertedSimilarity(byte[][] a, byte[][] b) {
               double s=0;
               float si;
               if (denominateur(a,b)==0)
                   return(2);
               s=1-(numerateur(a,b)/denominateur(a,b));
               si=(float)s;                
		return (si);
	}
        public static double moyenne(byte[][] a)
        {
            double x = 0;
             for (int i=0;i<a.length;i++)
             {
               for (int j=0;j<a[0].length;j++) 
                   x+=a[i][j];               
             }
            
            return(x/(a.length*a[0].length)); 
        }
        public static double denominateur(byte [][] a, byte [][] b)
        {
                int h=a.length,l=a[0].length;
                double x = 0, y=0;
                if ((h!=b.length)||(l!=b[0].length))
                {
                    System.out.println("il y a une erreur de parametres");
                    return 0f ;
                }
                for (int i=0;i<h;i++)
                {
                    for (int j=0;j<l;j++)
                     x+=Math.pow((a[i][j]-moyenne(a)),2);
                              
                }
                for (int i=0;i<h;i++)
                {
                    for (int j=0;j<l;j++)
                     y+=Math.pow((b[i][j]-moyenne(b)),2);
                              
                }
                return(Math.sqrt(x*y));
            
        }
        public static double numerateur(byte [][]a , byte [][] b)
        {   int h=a.length,l=a[0].length;
            double x = 0;
                if ((h!=b.length)||(l!=b[0].length))
                {
                    System.out.println("il y a une erreur de parametres");
                    return 0f ;
                }
                   
            
            for (int i=0;i<h;i++)
                {
                    for (int j=0;j<l;j++)
                        x+=((a[i][j]-moyenne(a))*(b[i][j]-moyenne(b)));
            
                }
            return(x);}

	/**
	 * @brief Quicksorts and returns the new indices of each value.
	 * 
	 * @param values the values whose indices have to be sorted in non decreasing
	 *               order
	 * 
	 * @return the array of sorted indices
	 * 
	 *         Example: values = quicksortIndices([3, 7, 0, 9]) gives [2, 0, 1, 3]*/
	 
	public static int[] quicksortIndices(float[] values) {
            int [] indices=new int[values.length];
            for (int i=0;i<values.length;i++)
                indices[i]=i;
            quicksortIndices(values,indices,0,values.length-1);
            return (indices);
	}

	/**
	 * @brief Sorts the provided values between two indices while applying the same
	 *        transformations to the array of indices
	 * 
	 * @param values  the values to sort
	 * @param indices the indices to sort according to the corresponding values
	 * @param         low, high are the **inclusive** bounds of the portion of array
	 *                to sort
	 */
	public static void quicksortIndices(float[] values, int[] indices, int low, int high) 
        {
            int l=low;
            int h=high;
            float pivot=values[low];
            while (l<=h)
            {
                if (values[l]<pivot)
                    l++;  
                else if (values[h]>pivot)
                    h--;                
                  else
                    {swap(l,h,values,indices);
                    l++;
                    h--;}
            }
            if (low<h) 
                quicksortIndices(values,indices,low,h);
            if (high>l) 
                quicksortIndices(values,indices,l,high); 
	}

	/**
	 * @brief Swaps the elements of the given arrays at the provided positions
	 * 
	 * @param         i, j the indices of the elements to swap
	 * @param values  the array floats whose values are to be swapped
	 * @param indices the array of ints whose values are to be swapped*/
	 
	public static void swap(int i, int j, float[] values, int[] indices) {
	
                int auxi=0 ;
                float auxv=0;
                if (values.length!=indices.length)
                      System.out.println("il y a une erreur de parametres"); 
                else
                    //indices
                    auxi=indices[i];
                    indices[i]=indices[j];
                    indices[j]=auxi;
                    //values
                    auxv=values[i];
                    values[i]=values[j];
                    values[j]=auxv;
   
	}

	/**
	 * @brief Returns the index of the largest element in the array
	 * 
	 * @param array an array of integers
	 * 
	 * @return the index of the largest integer*/
	 
	public static int indexOfMax(int[] array) {
            int max=array[0];
            int indice=0;
            for (int i=1;i<array.length;i++)
            {
                if (array[i]>max)
                {
                    max=array[i];
                    indice=i;
                }  
            }
		return(indice);
	}

	/**
	 * The k first elements of the provided array vote for a label
	 *
	 * @param sortedIndices the indices sorted by non-decreasing distance
	 * @param labels        the labels corresponding to the indices
	 * @param k             the number of labels asked to vote
	 *
	 * @return the winner of the election*/
	 
	public static byte electLabel(int[] sortedIndices, byte[] labels, int k) {
            int [] vote={0,0,0,0,0,0,0,0,0,0};
            int [] tab=new int[10];
            for (int i=0;i<10;i++)
            {
                tab[i]=sortedIndices.length;
            }                   
              for (int i=0;i<k;i++)
              {
                  switch(labels[sortedIndices[i]]){                     
                      case 0: vote[0]+=1;break;
                      case 1: vote[1]+=1;break;
                      case 2: vote[2]+=1;break;
                      case 3: vote[3]+=1;break;
                      case 4: vote[4]+=1;break;
                      case 5: vote[5]+=1;break;
                      case 6: vote[6]+=1;break;
                      case 7: vote[7]+=1;break;
                      case 8: vote[8]+=1;break;
                      case 9: vote[9]+=1;break;
                      default: System.out.println("il y a une erreur");
              }}
              for(int i=0;i<k;i++)
              {
                if(tab[labels[sortedIndices[i]]]==sortedIndices.length)
                {
                  tab[labels[sortedIndices[i]]]=i;
                }
               }
                   int max=vote[indexOfMax(vote)]  ;
                    int indicemin=indexOfMax(vote); 
                    int min = tab[indicemin] ;
                    for(int i=0;i<vote.length;i++)
                    {
                        if ((vote[i]==max)&&( tab[i]<min ))
                        {
                            
                            min = tab[i];
                            indicemin= i;
                            
                        }    
                    }
                    return((byte)indicemin);

	}

	/**
	 * Classifies the symbol drawn on the provided image
	 *
	 * @param image       the image to classify
	 * @param trainImages the tensor of training images
	 * @param trainLabels the list of labels corresponding to the training images
	 * @param k           the number of voters in the election process
	 *
	 * @return the label of the image*/
	 
	public static byte knnClassify(byte[][] image, byte[][][] trainImages, byte[] trainLabels, int k) {
		
        float [] tab=new float[trainImages.length];
        for(int i=0;i<trainImages.length;i++)
        {
            tab[i]=squaredEuclideanDistance(image,trainImages[i]);
        }
         

		return(electLabel(quicksortIndices(tab),trainLabels ,k));
	}

	/**
	 * Computes accuracy between two arrays of predictions
	 * 
	 * @param predictedLabels the array of labels predicted by the algorithm
	 * @param trueLabels      the array of true labels
	 * 
	 * @return the accuracy of the predictions. Its value is in [0, 1]*/
	 
	public static double accuracy(byte[] predictedLabels, byte[] trueLabels) {
        if (predictedLabels.length!=trueLabels.length)
        {
            System.out.println("il y a une erreur");
            return(0d);
        }
        int n=predictedLabels.length;
        double a=0;
        
        for(int i=0; i<n;i++)
        {
            if (predictedLabels[i]==trueLabels[i])
            {
                a=a+1;
            }
        }
		
		return (a/n);
	}


}
