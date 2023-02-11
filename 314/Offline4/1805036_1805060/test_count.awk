BEGIN {
	bond_count = 0;
}

{
    if ($1 == "A" && $2 == "H20") {
		bond_count++;
    }
}

END {

    Max_H2O = (Ox < (Hy/2)) ? Ox : (Hy/2);
    error = Max_H2O-bond_count;
    printf("Total H2O should be created: %d\n", Max_H2O);
    printf("Total H2O that  got  formed: %d\n", bond_count);
    printf("Total number of error: %d\n", error);
}
